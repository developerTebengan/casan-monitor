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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.casan.monitor.R
import id.casan.monitor.core.designsystem.components.*
import id.casan.monitor.core.designsystem.theme.*
import id.casan.monitor.data.local.MockIncidents

@Composable
fun DashboardScreen(state: MonitoringState, onAlerts: () -> Unit, retry: () -> Unit) {
    PageFrame { modifier ->
        LazyColumn(
            modifier,
            contentPadding = PaddingValues(Space.medium),
            verticalArrangement = Arrangement.spacedBy(Space.medium),
        ) {
            item { PageHeading(stringResource(R.string.overview), stringResource(R.string.overview_description)) }
            when (state) {
                MonitoringState.Loading -> item { LoadingState() }
                MonitoringState.Error -> item { ErrorState(retry) }
                is MonitoringState.Content -> {
                    item { SystemStatusCard(state.updatedAt) }
                    item { SectionHeader(stringResource(R.string.incident_summary)) }
                    item { IncidentSummary(state) }
                    item {
                        SectionHeader(stringResource(R.string.recent_alerts)) {
                            TextButton(onClick = onAlerts) { Text(stringResource(R.string.view_all)) }
                        }
                    }
                    if (state.recent.isEmpty()) {
                        item { EmptyState(stringResource(R.string.no_recent), stringResource(R.string.recent_empty)) }
                    }
                    items(state.recent, key = { it.id }) { IncidentCard(it) }
                }
            }
        }
    }
}

@Composable
private fun IncidentSummary(state: MonitoringState.Content) {
    val cards: @Composable (Modifier) -> Unit = { modifier ->
        IncidentSummaryCard(stringResource(R.string.critical_incidents), state.criticalCount, Critical, R.drawable.ic_warning, modifier)
        IncidentSummaryCard(stringResource(R.string.high_incidents), state.highCount, Warning, R.drawable.ic_bell, modifier)
        IncidentSummaryCard(stringResource(R.string.resolved_incidents), state.resolved.size, Success, R.drawable.ic_check, modifier)
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth >= 312.dp && LocalDensity.current.fontScale <= 1.3f) {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.small)) { cards(Modifier.weight(1f)) }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Space.small)) { cards(Modifier.fillMaxWidth()) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() {
    val snapshot = MockIncidents.snapshot()
    MonitorTheme {
        DashboardScreen(MonitoringState.Content(snapshot.incidents, snapshot.updatedAt), {}, {})
    }
}
