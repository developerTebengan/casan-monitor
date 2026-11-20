package id.casan.monitor.feature.notifications

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.casan.monitor.domain.model.MonitorNotification
import id.casan.monitor.domain.repository.NotificationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NotificationState {
    data object Loading : NotificationState
    data object Error : NotificationState
    data class Content(val items: List<MonitorNotification>) : NotificationState {
        val unreadCount = items.count { !it.isRead }
    }
}

sealed interface NotificationDetailState {
    data object Loading : NotificationDetailState
    data object Error : NotificationDetailState
    data class Content(val item: MonitorNotification?) : NotificationDetailState
}

@HiltViewModel
class NotificationCenterViewModel @Inject constructor(private val repository: NotificationRepository) : ViewModel() {
    private val retrySignal = MutableStateFlow(0)
    private val refreshingState = MutableStateFlow(false)
    val refreshing = refreshingState.asStateFlow()
    private val readFailure = MutableStateFlow<Pair<Long, String>?>(null)

    val readError = readFailure.asStateFlow()

    fun observe(accountId: Long): Flow<NotificationState> =
        reloadOnRequest(NotificationState.Error) {
            repository.observe(accountId).map { NotificationState.Content(it) }
        }.onEach { refreshingState.value = false }

    fun observeDetail(accountId: Long, id: String): Flow<NotificationDetailState> =
        reloadOnRequest(NotificationDetailState.Error) {
            repository.observeDetail(accountId, id).map { NotificationDetailState.Content(it) }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> reloadOnRequest(error: T, source: () -> Flow<T>): Flow<T> =
        retrySignal.flatMapLatest {
            source().catch { failure ->
                if (failure !is SQLiteException) throw failure
                emit(error)
            }
        }

    fun refresh(showProgress: Boolean = false) {
        if (showProgress) refreshingState.value = true
        retrySignal.update { it + 1 }
    }

    fun markRead(accountId: Long, notificationId: String) {
        readFailure.value = null
        viewModelScope.launch {
            try {
                repository.markRead(accountId, notificationId)
            } catch (_: SQLiteException) {
                readFailure.value = accountId to notificationId
            }
        }
    }
}
