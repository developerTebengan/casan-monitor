package id.casan.monitor.notification

import id.casan.monitor.domain.model.MonitorNotification
import id.casan.monitor.domain.model.NotificationCategory
import java.security.MessageDigest
import java.util.UUID

internal fun parseNotification(
    messageId: String?,
    title: String?,
    body: String?,
    type: String?,
    defaultTitle: String,
    receivedAt: Long,
): MonitorNotification? {
    val message = body?.trim()?.takeIf(String::isNotEmpty) ?: return null
    val headline = title?.trim()?.takeIf(String::isNotEmpty) ?: defaultTitle
    // Without an upstream ID, identical text may still represent distinct events.
    val identity = if (messageId.isNullOrBlank()) "local:${UUID.randomUUID()}" else "message:$messageId"
    val id = MessageDigest.getInstance("SHA-256").digest(identity.toByteArray()).joinToString("") { "%02x".format(it) }
    val category = NotificationCategory.CHARGING_QUEUE.takeIf { isQueueNotification(type) }
    return MonitorNotification(id, headline, message, category, receivedAt)
}

internal fun isNotificationId(value: String): Boolean =
    value.length == 64 && value.all { it in '0'..'9' || it in 'a'..'f' }
