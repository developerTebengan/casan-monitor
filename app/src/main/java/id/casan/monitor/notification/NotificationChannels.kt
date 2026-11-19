package id.casan.monitor.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import id.casan.monitor.R

fun createNotificationChannels(context: Context) {
    fun channel(id: Int, name: Int, importance: Int) =
        NotificationChannel(context.getString(id), context.getString(name), importance)

    context.getSystemService(NotificationManager::class.java).createNotificationChannels(
        listOf(
            channel(R.string.channel_critical_id, R.string.critical, NotificationManager.IMPORTANCE_HIGH),
            channel(R.string.channel_high_id, R.string.high, NotificationManager.IMPORTANCE_DEFAULT),
            channel(R.string.channel_info_id, R.string.info, NotificationManager.IMPORTANCE_LOW),
        ),
    )
}
