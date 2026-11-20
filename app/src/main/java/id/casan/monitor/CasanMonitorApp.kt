package id.casan.monitor

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import id.casan.monitor.notification.createNotificationChannels
import id.casan.monitor.notification.FirebaseDeviceSync
import javax.inject.Inject

@HiltAndroidApp
class CasanMonitorApp : Application() {
    @Inject lateinit var deviceSync: FirebaseDeviceSync

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels(this)
        deviceSync.start()
    }
}
