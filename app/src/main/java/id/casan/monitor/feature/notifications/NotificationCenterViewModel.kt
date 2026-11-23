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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NotificationState {
    data object Loading : NotificationState
    data object Error : NotificationState
    data class Content(
        val items: List<MonitorNotification>,
        val lastLoadedAt: Long? = null,
        val refreshFailed: Boolean = false,
    ) : NotificationState {
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
        observeNotificationList(accountId, repository, retrySignal)
            .onEach { refreshingState.value = false }

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

@OptIn(ExperimentalCoroutinesApi::class)
internal fun observeNotificationList(
    accountId: Long,
    repository: NotificationRepository,
    refreshes: Flow<Int>,
    now: () -> Long = System::currentTimeMillis,
): Flow<NotificationState> = flow {
    // Each collector starts with its own account snapshot.
    var lastLoaded: NotificationState.Content? = null
    refreshes.flatMapLatest {
        repository.observe(accountId)
            .map<List<MonitorNotification>, NotificationState> { NotificationState.Content(it, now()) }
            .catch { failure ->
                if (failure !is SQLiteException) throw failure
                emit(NotificationState.Error)
            }
    }.collect { result ->
        if (result is NotificationState.Content) lastLoaded = result
        emit(if (result == NotificationState.Error) lastLoaded?.copy(refreshFailed = true) ?: result else result)
    }
}
