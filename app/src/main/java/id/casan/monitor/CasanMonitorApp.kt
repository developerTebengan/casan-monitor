package id.casan.monitor

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import id.casan.monitor.notification.createNotificationChannels

@HiltAndroidApp
class CasanMonitorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels(this)
    }
}
