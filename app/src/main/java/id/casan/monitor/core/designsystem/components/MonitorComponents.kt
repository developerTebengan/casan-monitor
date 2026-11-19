package id.casan.monitor.core.designsystem.components

import android.text.format.DateUtils
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.casan.monitor.R
import id.casan.monitor.core.designsystem.theme.*
import id.casan.monitor.data.local.MockIncidents
import id.casan.monitor.domain.model.Incident
import id.casan.monitor.domain.model.IncidentSeverity
import id.casan.monitor.domain.model.IncidentStatus

private val CardShape = RoundedCornerShape(20.dp)
private val ScreenWidth = 680.dp

@Composable
fun MonitorIcon(@DrawableRes icon: Int, description: String? = null, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Icon(painterResource(icon), description, modifier, tint)
}

@Composable
fun MonitorTopAppBar(onAlerts: () -> Unit, onSettings: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Box(Modifier.fillMaxWidth().statusBarsPadding(), contentAlignment = Alignment.Center) {
            Row(
                Modifier.widthIn(max = ScreenWidth).fillMaxWidth()
                    .padding(horizontal = Space.medium, vertical = Space.small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.small),
            ) {
                BrandMark()
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                    Text(
                        stringResource(R.string.subtitle),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onAlerts) {
                    MonitorIcon(R.drawable.ic_bell, stringResource(R.string.alerts))
                }
                IconButton(onClick = onSettings) {
                    MonitorIcon(R.drawable.ic_settings, stringResource(R.string.settings))
                }
            }
        }
    }
}

@Composable
fun BrandMark() {
    Box(
        Modifier.size(Space.control).background(Ink, RoundedCornerShape(Space.medium)),
        contentAlignment = Alignment.Center,
    ) {
        MonitorIcon(R.drawable.ic_pulse, tint = Brand)
    }
}

@Composable
fun MonitorCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier,
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(Space.medium), verticalArrangement = Arrangement.spacedBy(Space.small), content = content)
    }
}

@Composable
fun SystemStatusCard(updatedAt: Long) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = CardShape,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(Space.large),
            verticalArrangement = Arrangement.spacedBy(Space.small),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.small), verticalAlignment = Alignment.CenterVertically) {
                MonitorIcon(R.drawable.ic_pulse)
                Text(stringResource(R.string.system_status), style = MaterialTheme.typography.labelLarge)
            }
            StatusIndicator(Success, stringResource(R.string.operational), prominent = true)
            Text(
                stringResource(R.string.updated, absoluteTime(updatedAt)),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun IncidentSummaryCard(title: String, count: Int, accent: Color, @DrawableRes icon: Int, modifier: Modifier = Modifier) {
    MonitorCard(modifier) {
        Box(
            Modifier.size(32.dp).background(accent.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            MonitorIcon(icon, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
        }
        Text(count.toString(), style = MaterialTheme.typography.headlineSmall)
        Text(title, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun IncidentCard(incident: Incident, absoluteTimestamp: Boolean = false) {
    MonitorCard(Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(Space.small), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                Text(incident.title, style = MaterialTheme.typography.titleMedium)
            }
            SeverityBadge(incident.severity)
        }
        Text(
            incident.description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Column(verticalArrangement = Arrangement.spacedBy(Space.small)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.small)) {
                Text(
                    incident.source,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    if (absoluteTimestamp) absoluteTime(incident.occurredAt) else DateUtils.getRelativeTimeSpanString(
                        incident.occurredAt, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
                    ).toString(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            val (statusLabel, statusColor) = when (incident.status) {
                IncidentStatus.OPEN -> R.string.open to MaterialTheme.colorScheme.outline
                IncidentStatus.ACKNOWLEDGED -> R.string.acknowledged to Warning
                IncidentStatus.RESOLVED -> R.string.resolved to Success
            }
            StatusIndicator(statusColor, stringResource(statusLabel))
        }
    }
}

@Composable
fun SeverityBadge(severity: IncidentSeverity) {
    val (label, accent) = when (severity) {
        IncidentSeverity.CRITICAL -> R.string.critical to Critical
        IncidentSeverity.HIGH -> R.string.high to Warning
        IncidentSeverity.INFO -> R.string.info to BrandDark
    }
    Surface(shape = CircleShape, color = accent.copy(alpha = 0.15f)) {
        Box(Modifier.padding(horizontal = Space.small, vertical = 4.dp)) {
            StatusIndicator(accent, stringResource(label))
        }
    }
}

@Composable
fun StatusIndicator(color: Color, label: String, prominent: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(Space.small), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Text(label, style = if (prominent) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun EmptyState(title: String, description: String) {
    MonitorCard(Modifier.fillMaxWidth()) {
        MonitorIcon(R.drawable.ic_bell)
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LoadingState() {
    val description = stringResource(R.string.loading)
    Box(Modifier.fillMaxWidth().padding(Space.large), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(Modifier.semantics { contentDescription = description })
    }
}

@Composable
fun ErrorState(retry: () -> Unit) {
    MonitorCard(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.load_error), style = MaterialTheme.typography.titleMedium)
        OutlinedButton(onClick = retry) { Text(stringResource(R.string.retry)) }
    }
}

@Composable
fun SectionHeader(title: String, action: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
        action?.invoke()
    }
}

@Composable
fun PageHeading(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun PageFrame(content: @Composable (Modifier) -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        content(Modifier.widthIn(max = ScreenWidth).fillMaxSize())
    }
}

@Composable
private fun absoluteTime(value: Long): String = DateUtils.formatDateTime(
    LocalContext.current, value, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME,
)

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ComponentPreview() {
    val snapshot = MockIncidents.snapshot()
    MonitorTheme {
        Surface {
            Column(Modifier.padding(Space.medium), verticalArrangement = Arrangement.spacedBy(Space.medium)) {
                SystemStatusCard(snapshot.updatedAt)
                IncidentCard(snapshot.incidents.first())
            }
        }
    }
}
