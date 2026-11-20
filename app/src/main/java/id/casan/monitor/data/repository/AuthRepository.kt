package id.casan.monitor.data.repository

import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import id.casan.monitor.BuildConfig
import id.casan.monitor.data.local.SessionManager
import id.casan.monitor.data.remote.AuthApi
import id.casan.monitor.data.remote.AuthEnvelope
import id.casan.monitor.data.remote.LoginRequest
import id.casan.monitor.data.remote.RefreshRequest
import id.casan.monitor.data.remote.toSession
import id.casan.monitor.domain.model.MonitorAccount
import id.casan.monitor.domain.model.MonitorSession
import id.casan.monitor.notification.MonitorDeviceRegistration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.io.InterruptedIOException
import javax.inject.Inject
import javax.inject.Singleton

enum class AuthError { CONFIGURATION, CREDENTIALS, TIMEOUT, NETWORK, SERVER, STORAGE }

sealed interface AuthState {
    data object Restoring : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val account: MonitorAccount, val expiresAt: Long) : AuthState
    data class Unavailable(val error: AuthError) : AuthState
}

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val storage: SessionManager,
    private val registration: MonitorDeviceRegistration,
    @ApplicationContext private val context: Context,
) {
    private val lock = Mutex()
    private val mutableState = MutableStateFlow<AuthState>(AuthState.Restoring)
    val state = mutableState.asStateFlow()
    private var session: MonitorSession? = null
    private var loaded = false

    suspend fun restore() = withContext(Dispatchers.IO + NonCancellable) {
        lock.withLock {
            if (!loaded) {
                session = storage.read()
                loaded = true
            }
            val current = session
            if (current == null) {
                mutableState.value = AuthState.SignedOut
            } else if (!current.isExpired()) {
                publish(current)
            } else {
                mutableState.value = AuthState.Restoring
                val result = request { api.refresh(RefreshRequest(current.refreshToken)) }
                when (result) {
                    is Reply.Success -> {
                        val renewed = result.payload.toSession(current.account)
                        val error = renewed?.let { save(it) }
                        if (renewed == null || error != null) {
                            clear()
                            mutableState.value = AuthState.Unavailable(error ?: AuthError.SERVER)
                        }
                    }
                    is Reply.Failure -> if (result.error == AuthError.CREDENTIALS) {
                        clear()
                    } else {
                        mutableState.value = AuthState.Unavailable(result.error)
                    }
                }
            }
        }
    }

    suspend fun login(email: String, password: String): AuthError? = withContext(Dispatchers.IO) {
        lock.withLock {
            if (BuildConfig.BACKEND_URL.isBlank()) return@withLock AuthError.CONFIGURATION
            if (mutableState.value is AuthState.SignedIn) return@withLock null
            when (val result = request { api.login(LoginRequest(email, password)) }) {
                is Reply.Failure -> result.error
                is Reply.Success -> {
                    val session = result.payload.toSession() ?: return@withLock AuthError.SERVER
                    save(session)
                }
            }
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO + NonCancellable) {
        val previous = lock.withLock {
            session.also { clear() }
        }
        if (previous != null) request { api.logout(RefreshRequest(previous.refreshToken)) }
        Unit
    }

    private fun clear() {
        storage.clear()
        session = null
        loaded = true
        mutableState.value = AuthState.SignedOut
        registration.clearAssociation()
        context.getSystemService(NotificationManager::class.java).cancelAll()
    }

    private fun publish(session: MonitorSession) {
        mutableState.value = AuthState.SignedIn(session.account, session.expiresAt)
    }

    private fun save(value: MonitorSession): AuthError? = try {
        storage.write(value)
        session = value
        loaded = true
        publish(value)
        null
    } catch (_: Exception) {
        AuthError.STORAGE
    }

    private sealed interface Reply<out T> {
        data class Success<T>(val payload: T) : Reply<T>
        data class Failure(val error: AuthError) : Reply<Nothing>
    }

    private suspend fun <T : Any> request(call: suspend () -> Response<T>): Reply<T> = try {
        val response = call()
        val body = response.body()
        if (response.isSuccessful && body != null) {
            Reply.Success(body)
        } else {
            response.errorBody()?.close()
            Reply.Failure(
                when (response.code()) {
                    401, 403 -> AuthError.CREDENTIALS
                    else -> AuthError.SERVER
                },
            )
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: InterruptedIOException) {
        Reply.Failure(AuthError.TIMEOUT)
    } catch (_: IOException) {
        Reply.Failure(AuthError.NETWORK)
    } catch (_: RuntimeException) {
        Reply.Failure(AuthError.SERVER)
    }
}
