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
import id.casan.monitor.BuildConfig
import id.casan.monitor.MainActivity
import id.casan.monitor.R
import id.casan.monitor.data.repository.AuthRepository
import id.casan.monitor.domain.model.MonitorNotification
import id.casan.monitor.domain.repository.NotificationRepository
import javax.inject.Inject

@AndroidEntryPoint
class CasanFirebaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var registration: MonitorDeviceRegistration
    @Inject lateinit var auth: AuthRepository
    @Inject lateinit var notifications: NotificationRepository
    @Inject lateinit var diagnostics: NotificationDiagnostics

    override fun onNewToken(token: String) {
        registration.tokenChanged()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // Firebase may display notification payloads in the background without invoking this callback.
        val receivedAt = System.currentTimeMillis()
        val activityResumed = diagnostics.activityResumed
        auth.withNotificationAccount { accountId ->
            val payload = message.notification
            val type = message.data["type"]
            val item = payload?.takeUnless { isQueueNotification(type) }?.let {
                parseNotification(message.messageId, it.title, it.body, type, getString(R.string.app_name), receivedAt)
            }
            val validation = when {
                isQueueNotification(type) -> PayloadResult.CUSTOMER_EVENT
                payload == null -> PayloadResult.UNSUPPORTED
                item == null -> PayloadResult.INVALID
                else -> PayloadResult.ACCEPTED
            }
            val receipt = if (BuildConfig.DEBUG) {
                NotificationReceipt(accountId, receivedAt, activityResumed, validation, item?.id?.take(12))
            } else null
            if (item != null) deliver(accountId, item, receipt)
            else if (receipt != null) diagnostics.record(receipt)
        }
    }

    private fun deliver(
        accountId: Long,
        item: MonitorNotification,
        receipt: NotificationReceipt?,
    ) {
        val cache = try {
            if (notifications.record(accountId, item)) CacheResult.SAVED else CacheResult.DUPLICATE
        } catch (_: SQLiteException) {
            CacheResult.FAILED
        }
        val cachedReceipt = receipt?.copy(cache = cache)
        if (cache == CacheResult.DUPLICATE) {
            cachedReceipt?.let(diagnostics::record)
            return
        }
        val manager = NotificationManagerCompat.from(this)
        val channel = getString(R.string.channel_info_id)
        val blocked = when {
            !manager.areNotificationsEnabled() -> TrayResult.APP_DISABLED
            getSystemService(NotificationManager::class.java).getNotificationChannel(channel)?.importance ==
                NotificationManager.IMPORTANCE_NONE -> TrayResult.CHANNEL_DISABLED
            else -> null
        }
        if (blocked != null) {
            cachedReceipt?.copy(channel = channel, tray = blocked)?.let(diagnostics::record)
            return
        }
        // A notification without a saved row can still inform the user and lead to Alerts.
        val target = notificationTapTarget(accountId, item.id, cached = cache == CacheResult.SAVED)
        val tap = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .setAction(MainActivity.ACTION_OPEN_NOTIFICATION)
                .setData(Uri.fromParts(NOTIFICATION_SCHEME, target, null))
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
        val tray = try {
            manager.notify(item.id, 0, notification)
            TrayResult.POSTED
        } catch (_: SecurityException) {
            TrayResult.PERMISSION_CHANGED
        }
        cachedReceipt?.copy(
            channel = channel,
            tray = tray,
            postedAt = System.currentTimeMillis().takeIf { tray == TrayResult.POSTED },
        )?.let(diagnostics::record)
    }
}
