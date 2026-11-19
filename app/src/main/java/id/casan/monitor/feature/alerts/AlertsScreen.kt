package id.casan.monitor.feature.alerts

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import id.casan.monitor.R
import id.casan.monitor.core.designsystem.components.*
import id.casan.monitor.core.designsystem.theme.MonitorTheme
import id.casan.monitor.core.designsystem.theme.Space
import id.casan.monitor.data.local.MockIncidents
import id.casan.monitor.domain.model.IncidentSeverity
import id.casan.monitor.feature.dashboard.AlertSearchState
import id.casan.monitor.feature.dashboard.MonitoringState

private enum class AlertFilter(val label: Int, val severity: IncidentSeverity?) {
    ALL(R.string.all, null),
    CRITICAL(R.string.critical, IncidentSeverity.CRITICAL),
    HIGH(R.string.high, IncidentSeverity.HIGH),
    INFO(R.string.info, IncidentSeverity.INFO),
}

@Composable
fun AlertsScreen(
    state: MonitoringState,
    search: AlertSearchState,
    onQuery: (String) -> Unit,
    onSeverity: (IncidentSeverity?) -> Unit,
    retry: () -> Unit,
) {
    PageFrame { modifier ->
        LazyColumn(
            modifier,
            contentPadding = PaddingValues(Space.medium),
            verticalArrangement = Arrangement.spacedBy(Space.medium),
        ) {
            item { PageHeading(stringResource(R.string.alerts), stringResource(R.string.alerts_intro)) }
            item {
                OutlinedTextField(
                    value = search.query,
                    onValueChange = onQuery,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.search_alerts)) },
                    leadingIcon = { MonitorIcon(R.drawable.ic_search) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                )
            }
            item {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Space.small),
                ) {
                    AlertFilter.entries.forEach { option ->
                        FilterChip(
                            selected = search.severity == option.severity,
                            onClick = { onSeverity(option.severity) },
                            label = { Text(stringResource(option.label)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        )
                    }
                }
            }
            when (state) {
                MonitoringState.Loading -> item { LoadingState() }
                MonitoringState.Error -> item { ErrorState(retry) }
                is MonitoringState.Content -> if (search.incidents.isEmpty()) {
                    item { EmptyState(stringResource(R.string.no_alerts), stringResource(R.string.adjust_filters)) }
                } else {
                    items(search.incidents, key = { it.id }) { IncidentCard(it) }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlertsPreview() {
    val snapshot = MockIncidents.snapshot()
    MonitorTheme {
        AlertsScreen(
            MonitoringState.Content(snapshot.incidents, snapshot.updatedAt),
            AlertSearchState(incidents = snapshot.incidents),
            {}, {}, {},
        )
    }
}
