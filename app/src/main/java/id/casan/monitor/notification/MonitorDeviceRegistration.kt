package id.casan.monitor.notification

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import id.casan.monitor.domain.model.MonitorAccount
import javax.inject.Inject
import javax.inject.Singleton

enum class RegistrationAvailability { ADMIN_REGISTRATION_UNAVAILABLE }

/**
 * Integration boundary for the device registration contract. The existing Firebase route
 * requires a platform user principal, while Monitor signs in with an admin principal.
 */
interface MonitorDeviceRegistration {
    fun availability(account: MonitorAccount): RegistrationAvailability
    fun tokenChanged()
    fun clearAssociation()
}

@Singleton
class AdminDeviceRegistration @Inject constructor(
    @ApplicationContext context: Context,
) : MonitorDeviceRegistration {
    private val preferences = context.getSharedPreferences("monitor_push", Context.MODE_PRIVATE)

    override fun availability(account: MonitorAccount) =
        RegistrationAvailability.ADMIN_REGISTRATION_UNAVAILABLE

    override fun tokenChanged() {
        preferences.edit().putBoolean("registration_pending", true).apply()
    }

    override fun clearAssociation() {
        preferences.edit().remove("registration_pending").apply()
    }
}
