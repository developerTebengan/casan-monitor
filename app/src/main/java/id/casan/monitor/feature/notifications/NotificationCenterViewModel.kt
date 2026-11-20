package id.casan.monitor.feature.notifications

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.casan.monitor.domain.model.MonitorNotification
import id.casan.monitor.domain.repository.NotificationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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

@HiltViewModel
class NotificationCenterViewModel @Inject constructor(private val repository: NotificationRepository) : ViewModel() {
    private val retrySignal = MutableStateFlow(0)
    private val readFailure = MutableStateFlow<Pair<Long, String>?>(null)

    val readError = readFailure.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(accountId: Long) = retrySignal.flatMapLatest {
        repository.observe(accountId)
            .map<List<MonitorNotification>, NotificationState> { NotificationState.Content(it) }
            .catch { failure ->
                if (failure !is SQLiteException) throw failure
                emit(NotificationState.Error)
            }
    }

    fun refresh() {
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
