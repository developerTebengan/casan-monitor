package id.casan.monitor.notification

import android.content.Context
import android.net.ConnectivityManager
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import id.casan.monitor.core.network.validatedConnectivity
import id.casan.monitor.data.repository.AuthRepository
import id.casan.monitor.data.repository.AuthState
import id.casan.monitor.data.repository.DeviceRegistrationResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class FirebaseDeviceSync @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: AuthRepository,
    private val registration: MonitorDeviceRegistration,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        scope.launch {
            val accounts = auth.state.map { (it as? AuthState.SignedIn)?.account?.id }.distinctUntilChanged()
            val connectivity = context.getSystemService(ConnectivityManager::class.java).validatedConnectivity()
            combine(accounts, connectivity, registration.requests) { account, online, _ -> account to online }
                .collectLatest { (account, online) ->
                    if (account != null) synchronize(account, online)
                }
        }
    }

    private suspend fun synchronize(accountId: Long, online: Boolean) {
        val generation = registration.generation()
        if (FirebaseApp.getApps(context).none { it.name == FirebaseApp.DEFAULT_APP_NAME }) {
            registration.report(generation, RegistrationStatus.FIREBASE_UNAVAILABLE)
            return
        }
        if (!online) {
            registration.report(generation, RegistrationStatus.OFFLINE)
            return
        }
        registration.report(generation, RegistrationStatus.REGISTERING)
        repeat(3) { attempt ->
            if (attempt > 0) delay(1_000L shl (attempt - 1))
            val token = try {
                withTimeoutOrNull(15_000) { FirebaseMessaging.getInstance().token.awaitResult() }
                    ?.takeIf(String::isNotBlank)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                null
            } ?: return@repeat

            if (registration.matches(accountId, token)) {
                registration.confirm(generation, accountId, token)
                return
            }
            when (auth.registerDevice(accountId, token)) {
                DeviceRegistrationResult.REGISTERED -> {
                    registration.confirm(generation, accountId, token)
                    return
                }
                DeviceRegistrationResult.REJECTED -> {
                    registration.report(generation, RegistrationStatus.FAILED)
                    return
                }
                DeviceRegistrationResult.SESSION_CHANGED -> return
                DeviceRegistrationResult.RETRY -> Unit
            }
        }
        registration.report(generation, RegistrationStatus.FAILED)
    }
}

internal suspend fun <T> Task<T>.awaitResult(): T =
    suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            when {
                task.isSuccessful -> continuation.resume(task.result)
                task.isCanceled -> continuation.cancel()
                else -> continuation.resumeWithException(task.exception ?: IllegalStateException("Firebase operation failed"))
            }
        }
    }
