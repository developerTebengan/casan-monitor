package id.casan.monitor.feature.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import id.casan.monitor.core.designsystem.theme.MonitorTheme
import id.casan.monitor.R
import id.casan.monitor.core.designsystem.components.*
import id.casan.monitor.core.designsystem.theme.Space
import id.casan.monitor.domain.model.MonitorNotification
import id.casan.monitor.domain.model.NotificationCategory
import id.casan.monitor.domain.model.NotificationSeverity
import id.casan.monitor.domain.model.filterNotifications
import java.text.DateFormat
import java.util.Date

@Composable
fun NotificationListScreen(
    state: NotificationState,
    history: Boolean,
    onRefresh: () -> Unit,
    onOpen: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var severityName by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryName by rememberSaveable { mutableStateOf<String?>(null) }
    val severity = NotificationSeverity.entries.firstOrNull { it.name == severityName }
    val category = NotificationCategory.entries.firstOrNull { it.name == categoryName }

    PageFrame { modifier ->
        LazyColumn(
            modifier,
            contentPadding = PaddingValues(Space.medium),
            verticalArrangement = Arrangement.spacedBy(Space.medium),
        ) {
            item {
                SectionHeader(stringResource(if (history) R.string.history else R.string.alerts)) {
                    TextButton(onClick = onRefresh) { Text("Refresh") }
                }
                if (history) Text("Notifications received on this device")
                else if (state is NotificationState.Content) Text("${state.unreadCount} unread")
            }
            if (!history) item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.search_alerts)) },
                    leadingIcon = { MonitorIcon(R.drawable.ic_search) },
                    singleLine = true,
                )
            }
            item {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Space.small),
                ) {
                    FilterChip(
                        selected = severity == null && category == null,
                        onClick = { severityName = null; categoryName = null },
                        label = { Text(stringResource(R.string.all)) },
                    )
                    if (history) {
                        NotificationCategory.entries.forEach { option ->
                            FilterChip(category == option, { categoryName = option.name }, { Text(option.label) })
                        }
                    } else {
                        NotificationSeverity.entries.forEach { option ->
                            FilterChip(severity == option, { severityName = option.name }, { Text(option.label) })
                        }
                    }
                }
            }
            when (state) {
                NotificationState.Loading -> item { LoadingState() }
                NotificationState.Error -> item { NotificationStorageError(onRefresh) }
                is NotificationState.Content -> {
                    val matching = state.items.filterNotifications(query, severity, category)
                    when {
                        state.items.isEmpty() -> item {
                            EmptyState("No notifications yet", "Messages received by the app will appear here.")
                        }
                        matching.isEmpty() -> item {
                            EmptyState("No matching notifications", "Try changing your search or filters.")
                        }
                        else -> items(matching, key = { it.id }) { NotificationCard(it, onOpen) }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: MonitorNotification, onOpen: (String) -> Unit) {
    val color = if (notification.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
    MonitorCard(Modifier.fillMaxWidth().clickable { onOpen(notification.id) }) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.small)) {
            MonitorIcon(R.drawable.ic_bell, tint = color)
            Text(
                notification.title,
                modifier = Modifier.weight(1f),
                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    notification.severity.label,
                    modifier = Modifier.padding(horizontal = Space.small),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        Text(notification.message, maxLines = 2, overflow = TextOverflow.Ellipsis)
        notification.category?.let { Text(it.label, style = MaterialTheme.typography.labelMedium) }
        Text(
            "${notificationTime(notification.receivedAt)} · ${if (notification.isRead) "Read" else "Unread"}",
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

@Composable
fun NotificationDetailScreen(
    state: NotificationState,
    id: String,
    readFailed: Boolean,
    onRead: () -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
) {
    val notification = (state as? NotificationState.Content)?.items?.firstOrNull { it.id == id }
    LaunchedEffect(notification?.id) {
        if (notification?.isRead == false) onRead()
    }
    PageFrame { modifier ->
        LazyColumn(
            modifier,
            contentPadding = PaddingValues(Space.medium),
            verticalArrangement = Arrangement.spacedBy(Space.medium),
        ) {
            item { TextButton(onClick = onBack) { Text("Back") } }
            when (state) {
                NotificationState.Loading -> item { LoadingState() }
                NotificationState.Error -> item { NotificationStorageError(onRefresh) }
                is NotificationState.Content -> item {
                    if (notification == null) {
                        EmptyState("Notification unavailable", "This notification is unavailable for this account on this device.")
                    } else {
                        MonitorCard(Modifier.fillMaxWidth()) {
                            Text(notification.title, style = MaterialTheme.typography.headlineSmall)
                            Text(notification.severity.label)
                            notification.category?.let { Text(it.label) }
                            Text(notificationTime(notification.receivedAt))
                            Text(if (notification.isRead) "Read" else "Unread")
                            HorizontalDivider()
                            Text(notification.message)
                            if (!notification.isRead && readFailed) {
                                Text("Could not save read status.")
                                TextButton(onClick = onRead) { Text("Retry") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationStorageError(retry: () -> Unit) {
    MonitorCard(Modifier.fillMaxWidth()) {
        Text("Could not load notifications", style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = retry) { Text("Retry") }
    }
}

@Composable
private fun notificationTime(timestamp: Long): String = remember(timestamp) {
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(timestamp))
}

@Preview(name = "Alerts in light mode", showBackground = true)
@Preview(name = "Alerts in dark mode", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotificationListPreview() {
    MonitorTheme(androidx.compose.foundation.isSystemInDarkTheme()) {
        NotificationListScreen(
            NotificationState.Content(
                listOf(MonitorNotification("preview", "Charging queue", "Your charger is ready.", NotificationCategory.CHARGING_QUEUE, 1735689600000)),
            ),
            history = false,
            onRefresh = {},
            onOpen = {},
        )
    }
}
