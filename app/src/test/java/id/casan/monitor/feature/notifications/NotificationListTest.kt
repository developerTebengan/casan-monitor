package id.casan.monitor.feature.notifications

import id.casan.monitor.domain.model.MonitorNotification
import id.casan.monitor.domain.model.NotificationSeverity
import id.casan.monitor.domain.model.filterNotifications
import id.casan.monitor.domain.repository.NotificationRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationListTest {
    private val alert = MonitorNotification("1", "Station offline", "Device unavailable", null, 100)

    @Test
    fun observedInsertsUpdateListAndUnreadCount() = runBlocking {
        val repository = CachedNotifications()
        val refreshes = MutableStateFlow(0)
        val states = Channel<NotificationState.Content>(Channel.UNLIMITED)
        val collector = launch {
            observeNotificationList(7, repository, refreshes, now = { 123L }).collect {
                states.send(it as NotificationState.Content)
            }
        }
        try {
            withTimeout(5_000) {
                assertTrue(states.receive().items.isEmpty())
                repository.record(7, alert)
                val update = states.receive()
                assertEquals(listOf(alert), update.items)
                assertEquals(1, update.unreadCount)
                assertEquals(123L, update.lastLoadedAt)
                repository.markRead(7, alert.id)
                assertEquals(0, states.receive().unreadCount)
            }
        } finally {
            collector.cancel()
        }
    }

    @Test
    fun explicitRefreshReloadsCurrentAccountCache() = runBlocking {
        val repository = CachedNotifications()
        repository.record(7, alert)
        repository.record(8, alert.copy(id = "2"))
        val refreshes = MutableStateFlow(0)
        val states = Channel<NotificationState.Content>(Channel.UNLIMITED)
        var clock = 100L
        val collector = launch {
            observeNotificationList(7, repository, refreshes, now = { ++clock }).collect {
                states.send(it as NotificationState.Content)
            }
        }
        try {
            withTimeout(5_000) {
                val initial = states.receive()
                refreshes.value++
                val refreshed = states.receive()
                assertEquals(listOf(alert), refreshed.items)
                assertEquals(initial.lastLoadedAt!! + 1, refreshed.lastLoadedAt)
                assertEquals(2, repository.observations)
            }
        } finally {
            collector.cancel()
        }
    }

    @Test
    fun searchMatchesTitleOrDescriptionAndCombinesWithSeverity() {
        val alerts = listOf(alert, alert.copy(id = "2", title = "Update", message = "Station online"))
        assertEquals(alerts, alerts.filterNotifications(query = " Station ", severity = NotificationSeverity.INFO))
        assertEquals(listOf(alert), alerts.filterNotifications(query = "OFFLINE"))
        assertTrue(alerts.filterNotifications(query = "Station", severity = NotificationSeverity.HIGH).isEmpty())
        assertTrue(alerts.filterNotifications(query = "unknown").isEmpty())
    }

    private class CachedNotifications : NotificationRepository {
        private val rows = MutableStateFlow<Map<Long, List<MonitorNotification>>>(emptyMap())
        var observations = 0
            private set

        override fun observe(accountId: Long) = rows.map { it[accountId].orEmpty() }.also { observations++ }

        override fun observeDetail(accountId: Long, notificationId: String) =
            rows.map { scoped -> scoped[accountId]?.find { it.id == notificationId } }

        override fun record(accountId: Long, notification: MonitorNotification): Boolean {
            val alerts = rows.value[accountId].orEmpty()
            if (alerts.any { it.id == notification.id }) return false
            rows.value += accountId to (alerts + notification)
            return true
        }

        override suspend fun markRead(accountId: Long, notificationId: String) {
            rows.value += accountId to rows.value[accountId].orEmpty().map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            }
        }
    }
}
