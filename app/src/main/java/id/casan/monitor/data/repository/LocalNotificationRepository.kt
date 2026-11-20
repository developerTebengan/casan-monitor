package id.casan.monitor.data.repository

import id.casan.monitor.data.local.NotificationDatabase
import id.casan.monitor.domain.model.MonitorNotification
import id.casan.monitor.domain.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalNotificationRepository @Inject constructor(private val database: NotificationDatabase) : NotificationRepository {
    private val revision = MutableStateFlow(0)

    override fun observe(accountId: Long) =
        revision.map { database.list(accountId) }.flowOn(Dispatchers.IO)

    override fun observeDetail(accountId: Long, notificationId: String) =
        revision.map { database.detail(accountId, notificationId) }.flowOn(Dispatchers.IO)

    // Called on FirebaseMessagingService's worker thread.
    override fun record(accountId: Long, notification: MonitorNotification): Boolean =
        database.insert(accountId, notification).also { inserted ->
            if (inserted) revision.update { it + 1 }
        }

    override suspend fun markRead(accountId: Long, notificationId: String) = withContext(Dispatchers.IO) {
        if (database.markRead(accountId, notificationId)) revision.update { it + 1 }
    }
}
