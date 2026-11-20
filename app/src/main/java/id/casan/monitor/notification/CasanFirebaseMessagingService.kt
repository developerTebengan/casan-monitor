package id.casan.monitor.notification

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import id.casan.monitor.MainActivity
import id.casan.monitor.R
import id.casan.monitor.data.repository.AuthRepository
import id.casan.monitor.data.repository.AuthState
import javax.inject.Inject

@AndroidEntryPoint
class CasanFirebaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var registration: MonitorDeviceRegistration
    @Inject lateinit var auth: AuthRepository

    override fun onNewToken(token: String) {
        registration.tokenChanged()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // Firebase displays backend notification payloads itself when the app is in the background.
        val payload = message.notification ?: return
        val session = auth.state.value as? AuthState.SignedIn ?: return
        if (session.expiresAt <= System.currentTimeMillis()) return
        val title = payload.title?.takeIf(String::isNotBlank) ?: getString(R.string.app_name)
        val body = payload.body?.takeIf(String::isNotBlank) ?: return
        val manager = NotificationManagerCompat.from(this)
        if (!manager.areNotificationsEnabled()) return
        val tap = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .setAction(MainActivity.ACTION_OPEN_NOTIFICATION)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, getString(R.string.channel_info_id))
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(tap)
            .setAutoCancel(true)
            .build()
        val tag = message.messageId ?: "${message.sentTime}:${title.hashCode()}:${body.hashCode()}"
        try {
            manager.notify(tag, 0, notification)
        } catch (_: SecurityException) {
            // Permission can be revoked between the check and the notification.
        }
    }
}
