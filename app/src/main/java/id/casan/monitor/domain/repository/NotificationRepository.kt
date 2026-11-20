package id.casan.monitor.domain.repository

import id.casan.monitor.domain.model.MonitorNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observe(accountId: Long): Flow<List<MonitorNotification>>
    fun observeDetail(accountId: Long, notificationId: String): Flow<MonitorNotification?>
    fun record(accountId: Long, notification: MonitorNotification): Boolean
    suspend fun markRead(accountId: Long, notificationId: String)
}
