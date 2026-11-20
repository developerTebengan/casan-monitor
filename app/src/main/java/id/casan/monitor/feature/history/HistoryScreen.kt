package id.casan.monitor.feature.history

import androidx.compose.runtime.Composable
import id.casan.monitor.feature.notifications.NotificationListScreen
import id.casan.monitor.feature.notifications.NotificationState

@Composable
fun HistoryScreen(state: NotificationState, onRefresh: () -> Unit, onOpen: (String) -> Unit) {
    NotificationListScreen(state, history = true, onRefresh = onRefresh, onOpen = onOpen)
}
