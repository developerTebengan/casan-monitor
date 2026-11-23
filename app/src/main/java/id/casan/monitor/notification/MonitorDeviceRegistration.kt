package id.casan.monitor.notification

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import id.casan.monitor.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

enum class RegistrationStatus { WAITING, OFFLINE, REGISTERING, REGISTERED, FIREBASE_UNAVAILABLE, FAILED }

@Singleton
class MonitorDeviceRegistration @Inject constructor(@ApplicationContext context: Context) {
    private val preferences = context.getSharedPreferences("monitor_push", Context.MODE_PRIVATE)
    private val mutableStatus = MutableStateFlow(RegistrationStatus.WAITING)
    val status = mutableStatus.asStateFlow()
    private val mutableRequests = MutableStateFlow(0)
    val requests = mutableRequests.asStateFlow()
    private var epoch = 0L

    @Synchronized
    fun tokenChanged() {
        epoch++
        requestSync()
    }

    fun requestSync() {
        mutableRequests.update { it + 1 }
    }

    @Synchronized
    fun generation(): Long = epoch

    @Synchronized
    fun report(generation: Long, status: RegistrationStatus) {
        if (generation == epoch) mutableStatus.value = status
    }

    @Synchronized
    fun matches(accountId: Long, token: String): Boolean =
        isAssociated(accountId) && preferences.getString("fingerprint", null) == fingerprint(token)

    fun isAssociated(accountId: Long): Boolean =
        preferences.getString("owner", null) == owner(accountId)

    @Synchronized
    fun confirm(generation: Long, accountId: Long, token: String) {
        if (generation != epoch) return
        preferences.edit().putString("owner", owner(accountId)).putString("fingerprint", fingerprint(token))
            .apply()
        mutableStatus.value = RegistrationStatus.REGISTERED
    }

    @Synchronized
    fun clearAssociation() {
        epoch++
        preferences.edit().remove("owner").remove("fingerprint").apply()
        mutableStatus.value = RegistrationStatus.WAITING
    }

    private fun owner(accountId: Long) = "${BuildConfig.BACKEND_URL}|admin|$accountId"

    private fun fingerprint(token: String) =
        MessageDigest.getInstance("SHA-256").digest(token.toByteArray()).joinToString("") { "%02x".format(it) }
}
