package id.casan.monitor.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import id.casan.monitor.MainActivity
import id.casan.monitor.R
import id.casan.monitor.data.repository.AuthRepository
import id.casan.monitor.data.repository.AuthState
import id.casan.monitor.domain.repository.NotificationRepository
import javax.inject.Inject

@AndroidEntryPoint
class CasanFirebaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var registration: MonitorDeviceRegistration
    @Inject lateinit var auth: AuthRepository
    @Inject lateinit var notifications: NotificationRepository

    override fun onNewToken(token: String) {
        registration.tokenChanged()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // Firebase displays backend notification payloads itself when the app is in the background.
        val payload = message.notification ?: return
        val session = auth.state.value as? AuthState.SignedIn ?: return
        if (session.expiresAt <= System.currentTimeMillis() || !registration.isAssociated(session.account.id)) return
        if (isQueueNotification(message.data["type"]) && message.data["user_id"]?.toLongOrNull() != session.account.id) return
        val item = parseNotification(
            message.messageId, payload.title, payload.body, message.data["type"],
            getString(R.string.app_name), System.currentTimeMillis(),
        ) ?: return
        val inserted = try {
            notifications.record(session.account.id, item)
        } catch (_: SQLiteException) {
            // Still display the system notification when local storage is unavailable.
            true
        }
        if (!inserted || auth.state.value != session) return
        val manager = NotificationManagerCompat.from(this)
        val channel = getString(R.string.channel_info_id)
        if (!manager.areNotificationsEnabled() ||
            getSystemService(NotificationManager::class.java).getNotificationChannel(channel)?.importance == NotificationManager.IMPORTANCE_NONE
        ) return
        val tap = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .setAction(MainActivity.ACTION_OPEN_NOTIFICATION)
                .setData(Uri.fromParts(NOTIFICATION_SCHEME, "user/${session.account.id}/${item.id}", null))
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, channel)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(item.title)
            .setContentText(item.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(item.message))
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(tap)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()
        try {
            manager.notify(item.id, 0, notification)
        } catch (_: SecurityException) {
            // Permission can be revoked between the check and the notification.
        }
    }
}
