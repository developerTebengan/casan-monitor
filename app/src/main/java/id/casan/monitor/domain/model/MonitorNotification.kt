package id.casan.monitor.domain.model

const val NOTIFICATION_WINDOW_SIZE = 250

enum class NotificationSeverity(val label: String) {
    CRITICAL("Critical"), HIGH("High"), MEDIUM("Medium"), INFO("Info"),
}

enum class NotificationCategory(val label: String) {
    CHARGING_QUEUE("Charging queue"),
}

data class MonitorNotification(
    val id: String,
    val title: String,
    val message: String,
    val category: NotificationCategory?,
    val receivedAt: Long,
    val isRead: Boolean = false,
) {
    // The current payload has no severity or source field. Use Info as the display priority.
    val severity: NotificationSeverity get() = NotificationSeverity.INFO
}

fun List<MonitorNotification>.filterNotifications(
    query: String = "",
    severity: NotificationSeverity? = null,
    category: NotificationCategory? = null,
): List<MonitorNotification> {
    val term = query.trim()
    return filter {
        (severity == null || it.severity == severity) &&
            (category == null || it.category == category) &&
            (it.title.contains(term, ignoreCase = true) || it.message.contains(term, ignoreCase = true))
    }
}
