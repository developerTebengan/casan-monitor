package id.casan.monitor.notification

import id.casan.monitor.domain.model.MonitorNotification
import id.casan.monitor.domain.model.NotificationCategory
import java.security.MessageDigest
import java.util.UUID

internal fun parseNotification(
    messageId: String?,
    sentAt: Long,
    title: String?,
    body: String?,
    type: String?,
    defaultTitle: String,
    receivedAt: Long,
): MonitorNotification? {
    val message = body?.trim()?.takeIf(String::isNotEmpty) ?: return null
    val headline = title?.trim()?.takeIf(String::isNotEmpty) ?: defaultTitle
    val identity = when {
        !messageId.isNullOrBlank() -> "message:$messageId"
        sentAt > 0 -> "fallback:$sentAt:$headline:$message:$type"
        else -> UUID.randomUUID().toString()
    }
    val id = MessageDigest.getInstance("SHA-256").digest(identity.toByteArray()).joinToString("") { "%02x".format(it) }
    val category = when (type) {
        "queue_called", "queue_expired" -> NotificationCategory.CHARGING_QUEUE
        else -> null
    }
    return MonitorNotification(id, headline, message, category, receivedAt)
}

internal fun isNotificationId(value: String): Boolean =
    value.length == 64 && value.all { it in '0'..'9' || it in 'a'..'f' }
