package id.casan.monitor.feature.alerts

import androidx.compose.runtime.Composable
import id.casan.monitor.feature.notifications.NotificationListScreen
import id.casan.monitor.feature.notifications.NotificationState

@Composable
fun AlertsScreen(
    state: NotificationState,
    refreshing: Boolean,
    offline: Boolean,
    onRefresh: () -> Unit,
    onOpen: (String) -> Unit,
) {
    NotificationListScreen(state, history = false, refreshing = refreshing, offline = offline, onRefresh = onRefresh, onOpen = onOpen)
}
