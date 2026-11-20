package id.casan.monitor.data.repository

import android.app.NotificationManager
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import id.casan.monitor.BuildConfig
import id.casan.monitor.data.local.SessionManager
import id.casan.monitor.data.remote.AuthApi
import id.casan.monitor.data.remote.AuthEnvelope
import id.casan.monitor.data.remote.FirebaseTokenRequest
import id.casan.monitor.data.remote.LoginRequest
import id.casan.monitor.data.remote.RefreshRequest
import id.casan.monitor.data.remote.SendOtpRequest
import id.casan.monitor.data.remote.toAccount
import id.casan.monitor.data.remote.toSession
import id.casan.monitor.domain.model.MonitorAccount
import id.casan.monitor.domain.model.MonitorSession
import id.casan.monitor.notification.MonitorDeviceRegistration
import id.casan.monitor.notification.awaitResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.Response
import java.io.IOException
import java.io.InterruptedIOException
import javax.inject.Inject
import javax.inject.Singleton

enum class AuthError { CONFIGURATION, CREDENTIALS, RATE_LIMIT, FORBIDDEN, TIMEOUT, NETWORK, SERVER, STORAGE }

enum class DeviceRegistrationResult { REGISTERED, RETRY, REJECTED, SESSION_CHANGED }

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
                    is Reply.Failure -> if (result.error == AuthError.CREDENTIALS || result.error == AuthError.FORBIDDEN) {
                        clear()
                    } else {
                        mutableState.value = AuthState.Unavailable(result.error)
                    }
                }
            }
        }
    }

    suspend fun sendOtp(phone: String, channel: Int): AuthError? {
        if (BuildConfig.BACKEND_URL.isBlank()) return AuthError.CONFIGURATION
        return when (val result = request { api.sendOtp(SendOtpRequest(phone, channel)) }) {
            is Reply.Failure -> result.error
            is Reply.Success -> if (result.payload.status == "success") null else AuthError.SERVER
        }
    }

    suspend fun login(phone: String, code: String, channel: Int): AuthError? = withContext(Dispatchers.IO) {
        lock.withLock {
            if (BuildConfig.BACKEND_URL.isBlank()) return@withLock AuthError.CONFIGURATION
            if (mutableState.value is AuthState.SignedIn) return@withLock null
            when (val result = request { api.login(LoginRequest(phone, code, channel)) }) {
                is Reply.Failure -> result.error
                is Reply.Success -> {
                    val access = result.payload.data?.token?.takeIf { result.payload.status == "success" && it.isNotBlank() }
                        ?: return@withLock AuthError.SERVER
                    val profile = when (val reply = request { api.profile("Bearer $access") }) {
                        is Reply.Failure -> return@withLock reply.error
                        is Reply.Success -> reply.payload.toAccount() ?: return@withLock AuthError.SERVER
                    }
                    val session = result.payload.toSession(profile.copy(phone = profile.phone.ifBlank { phone }))
                        ?: return@withLock AuthError.SERVER
                    save(session)
                }
            }
        }
    }

    suspend fun registerDevice(accountId: Long, token: String): DeviceRegistrationResult =
        withContext(Dispatchers.IO + NonCancellable) {
            lock.withLock {
                val current = session?.takeIf { it.account.id == accountId && !it.isExpired() }
                    ?: return@withLock DeviceRegistrationResult.SESSION_CHANGED
                when (val response = request { api.registerDevice("Bearer ${current.accessToken}", FirebaseTokenRequest(token)) }) {
                    is Reply.Success -> if (response.payload.status == "success") {
                        DeviceRegistrationResult.REGISTERED
                    } else {
                        DeviceRegistrationResult.REJECTED
                    }
                    is Reply.Failure -> when (response.error) {
                        AuthError.CREDENTIALS -> {
                            clear()
                            DeviceRegistrationResult.SESSION_CHANGED
                        }
                        AuthError.TIMEOUT, AuthError.NETWORK, AuthError.SERVER, AuthError.RATE_LIMIT -> DeviceRegistrationResult.RETRY
                        else -> DeviceRegistrationResult.REJECTED
                    }
                }
            }
        }

    suspend fun logout() = withContext(Dispatchers.IO + NonCancellable) {
        val previous = lock.withLock {
            val current = session
            clear()
            if (current != null && FirebaseApp.getApps(context).any { it.name == FirebaseApp.DEFAULT_APP_NAME }) {
                try {
                    withTimeoutOrNull(5_000) { FirebaseMessaging.getInstance().deleteToken().awaitResult() }
                } catch (_: Exception) {
                    // Local sign-out succeeds even when Firebase is unavailable.
                }
            }
            current
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
                    401 -> AuthError.CREDENTIALS
                    403 -> AuthError.FORBIDDEN
                    429 -> AuthError.RATE_LIMIT
                    in 400..499 -> AuthError.FORBIDDEN
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
