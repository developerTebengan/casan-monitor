package id.casan.monitor.feature.history

import androidx.compose.runtime.Composable
import id.casan.monitor.feature.notifications.NotificationListScreen
import id.casan.monitor.feature.notifications.NotificationState

@Composable
fun HistoryScreen(
    state: NotificationState,
    refreshing: Boolean,
    offline: Boolean,
    onRefresh: () -> Unit,
    onOpen: (String) -> Unit,
) {
    NotificationListScreen(state, history = true, refreshing = refreshing, offline = offline, onRefresh = onRefresh, onOpen = onOpen)
}
