package id.casan.monitor.notification

import id.casan.monitor.domain.model.NotificationCategory
import id.casan.monitor.domain.model.NotificationSeverity
import id.casan.monitor.domain.model.filterNotifications
import id.casan.monitor.feature.notifications.NotificationState
import org.junit.Assert.*
import org.junit.Test

class NotificationPayloadTest {
    @Test
    fun mapsVerifiedQueueMetadataAndDefaultsDisplayPriority() {
        val item = requireNotNull(payload(type = "queue_called"))
        assertEquals("Queue update", item.title)
        assertEquals("Your charger is ready", item.message)
        assertEquals(1234L, item.receivedAt)
        assertEquals(NotificationCategory.CHARGING_QUEUE, item.category)
        assertEquals(NotificationSeverity.INFO, item.severity)
        assertFalse(item.isRead)
    }

    @Test
    fun rejectsMissingOrBlankBodiesAndHandlesMissingTitle() {
        assertNull(payload(body = null))
        assertNull(payload(body = "   "))
        assertEquals("CASAN Monitor", payload(title = null)?.title)
        assertNull(payload(type = "unrecognized_event")?.category)
    }

    @Test
    fun redeliveryIdentityIsStableAndSafeForNavigation() {
        val first = requireNotNull(payload())
        val second = requireNotNull(payload(title = "Another headline"))
        assertEquals(first.id, second.id)
        assertTrue(isNotificationId(first.id))
        assertFalse(isNotificationId("../settings"))
        assertNotEquals(first.id, payload(id = "another/message")?.id)
        assertNotEquals(payload(id = null)?.id, payload(id = null)?.id)
    }

    @Test
    fun listFiltersAndUnreadCountsReflectReadState() {
        val queue = requireNotNull(payload(type = "queue_expired"))
        val other = requireNotNull(payload(id = "general", title = "Update", body = "Scheduled maintenance"))
        val items = listOf(queue, other)
        assertEquals(0, NotificationState.Content(emptyList()).unreadCount)
        assertEquals(2, NotificationState.Content(items).unreadCount)
        assertEquals(1, NotificationState.Content(listOf(queue.copy(isRead = true), other)).unreadCount)
        assertEquals(listOf(queue), items.filterNotifications(query = " CHARGER "))
        assertEquals(listOf(queue), items.filterNotifications(category = NotificationCategory.CHARGING_QUEUE))
        assertEquals(items, items.filterNotifications(severity = NotificationSeverity.INFO))
        assertTrue(items.filterNotifications(severity = NotificationSeverity.HIGH).isEmpty())
    }

    @Test
    fun tapTargetsRequireAValidIdAndMatchTheSignedInAccount() {
        val id = requireNotNull(payload()).id
        assertEquals(id, notificationDetailFor("user/42/$id", 42))
        assertNull(notificationDetailFor("user/42/$id", 17))
        assertNull(notificationDetailFor("invalid/$id", 42))
        assertNull(notificationDetailFor("user/42/../settings", 42))
        assertNull(notificationDetailFor("", 42))
        assertNull(notificationDetailFor(id, 42))
        assertNull(notificationDetailFor("42/$id", 42))
    }

    @Test
    fun onlyDocumentedQueueTypesRouteToAlerts() {
        assertTrue(isQueueNotification("queue_called"))
        assertTrue(isQueueNotification("queue_expired"))
        assertFalse(isQueueNotification("queue_unknown"))
        assertFalse(isQueueNotification(null))
    }

    private fun payload(
        id: String? = "server/message",
        title: String? = "Queue update",
        body: String? = "Your charger is ready",
        type: String? = null,
    ) = parseNotification(id, title, body, type, "CASAN Monitor", 1234)
}
