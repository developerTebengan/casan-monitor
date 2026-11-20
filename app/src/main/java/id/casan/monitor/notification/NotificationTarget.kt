package id.casan.monitor.notification

internal const val NOTIFICATION_SCHEME = "casan-notification"

internal fun notificationDetailFor(target: String, accountId: Long): String? {
    val prefix = "user/$accountId/"
    if (!target.startsWith(prefix)) return null
    return target.removePrefix(prefix).takeIf(::isNotificationId)
}

internal fun isQueueNotification(type: String?): Boolean =
    type == "queue_called" || type == "queue_expired"
