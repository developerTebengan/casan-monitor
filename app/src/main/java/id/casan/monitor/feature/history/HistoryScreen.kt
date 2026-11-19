package id.casan.monitor.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import id.casan.monitor.R
import id.casan.monitor.core.designsystem.components.*
import id.casan.monitor.core.designsystem.theme.MonitorTheme
import id.casan.monitor.core.designsystem.theme.Space
import id.casan.monitor.data.local.MockIncidents
import id.casan.monitor.feature.dashboard.MonitoringState

@Composable
fun HistoryScreen(state: MonitoringState, retry: () -> Unit) {
    PageFrame { modifier ->
        LazyColumn(
            modifier,
            contentPadding = PaddingValues(Space.medium),
            verticalArrangement = Arrangement.spacedBy(Space.medium),
        ) {
            item { PageHeading(stringResource(R.string.history), stringResource(R.string.history_intro)) }
            when (state) {
                MonitoringState.Loading -> item { LoadingState() }
                MonitoringState.Error -> item { ErrorState(retry) }
                is MonitoringState.Content -> if (state.resolved.isEmpty()) {
                    item { EmptyState(stringResource(R.string.no_history), stringResource(R.string.history_empty)) }
                } else {
                    items(state.resolved, key = { it.id }) { IncidentCard(it, absoluteTimestamp = true) }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryPreview() {
    val snapshot = MockIncidents.snapshot()
    MonitorTheme { HistoryScreen(MonitoringState.Content(snapshot.incidents, snapshot.updatedAt), {}) }
}
