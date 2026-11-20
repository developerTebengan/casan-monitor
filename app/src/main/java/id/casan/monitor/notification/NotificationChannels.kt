package id.casan.monitor.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import id.casan.monitor.R

fun createNotificationChannels(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java)

    fun channel(id: Int, name: Int, importance: Int): NotificationChannel? {
        val channelId = context.getString(id)
        if (manager.getNotificationChannel(channelId) != null) return null
        return NotificationChannel(channelId, context.getString(name), importance).apply {
            enableVibration(importance == NotificationManager.IMPORTANCE_HIGH)
        }
    }

    manager.createNotificationChannels(
        listOfNotNull(
            channel(R.string.channel_critical_id, R.string.critical, NotificationManager.IMPORTANCE_HIGH),
            channel(R.string.channel_high_id, R.string.high, NotificationManager.IMPORTANCE_HIGH),
            channel(R.string.channel_info_id, R.string.info, NotificationManager.IMPORTANCE_DEFAULT),
        ),
    )
}
