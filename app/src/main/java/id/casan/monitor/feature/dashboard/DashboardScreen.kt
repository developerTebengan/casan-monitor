package id.casan.monitor.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import id.casan.monitor.R
import id.casan.monitor.core.designsystem.components.*
import id.casan.monitor.core.designsystem.theme.*
import id.casan.monitor.domain.model.NotificationSeverity
import id.casan.monitor.feature.notifications.NotificationCard
import id.casan.monitor.feature.notifications.NotificationState
import id.casan.monitor.feature.notifications.NotificationStorageError

@Composable
fun DashboardScreen(state: NotificationState, offline: Boolean, onAlerts: () -> Unit, retry: () -> Unit, onOpen: (String) -> Unit) {
    PageFrame { modifier ->
        LazyColumn(
            modifier,
            contentPadding = PaddingValues(Space.medium),
            verticalArrangement = Arrangement.spacedBy(Space.medium),
        ) {
            item {
                PageHeading(stringResource(R.string.overview), "Notifications received on this device")
                if (offline) Text("Offline · Saved notifications")
                TextButton(onClick = retry) { Text("Refresh") }
            }
            when (state) {
                NotificationState.Loading -> item { LoadingState() }
                NotificationState.Error -> item { NotificationStorageError(retry) }
                is NotificationState.Content -> {
                    item { NotificationSummary(state) }
                    item {
                        SectionHeader("Recent notifications") {
                            TextButton(onClick = onAlerts) { Text(stringResource(R.string.view_all)) }
                        }
                    }
                    if (state.items.isEmpty()) {
                        item { EmptyState("No recent notifications", "Messages received by the app will appear here.") }
                    }
                    items(state.items.take(4), key = { it.id }) { NotificationCard(it, onOpen) }
                }
            }
        }
    }
}

@Composable
private fun NotificationSummary(state: NotificationState.Content) {
    val cards: @Composable (Modifier) -> Unit = { modifier ->
        IncidentSummaryCard("Unread", state.unreadCount, Success, R.drawable.ic_bell, modifier)
        IncidentSummaryCard("Critical", state.items.count { it.severity == NotificationSeverity.CRITICAL }, Critical, R.drawable.ic_warning, modifier)
        IncidentSummaryCard("High", state.items.count { it.severity == NotificationSeverity.HIGH }, Warning, R.drawable.ic_bell, modifier)
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth >= 312.dp && LocalDensity.current.fontScale <= 1.3f) {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.small)) { cards(Modifier.weight(1f)) }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Space.small)) { cards(Modifier.fillMaxWidth()) }
        }
    }
}
