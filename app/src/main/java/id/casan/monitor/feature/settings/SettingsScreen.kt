package id.casan.monitor.feature.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import id.casan.monitor.BuildConfig
import id.casan.monitor.R
import id.casan.monitor.core.designsystem.components.*
import id.casan.monitor.core.designsystem.theme.MonitorTheme
import id.casan.monitor.core.designsystem.theme.Space
import id.casan.monitor.data.local.ThemeMode

@Composable
fun SettingsScreen(mode: ThemeMode, onTheme: (ThemeMode) -> Unit, onNotifications: () -> Unit) {
    PageFrame { modifier ->
        LazyColumn(
            modifier,
            contentPadding = PaddingValues(Space.medium),
            verticalArrangement = Arrangement.spacedBy(Space.medium),
        ) {
            item { Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium) }
            item {
                SectionHeader(stringResource(R.string.appearance))
            }
            item {
                MonitorCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium)
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Space.small),
                    ) {
                        ThemeMode.entries.forEach { option ->
                            val label = when (option) {
                                ThemeMode.SYSTEM -> R.string.theme_system
                                ThemeMode.LIGHT -> R.string.theme_light
                                ThemeMode.DARK -> R.string.theme_dark
                            }
                            FilterChip(selected = mode == option, onClick = { onTheme(option) }, label = { Text(stringResource(label)) })
                        }
                    }
                }
            }
            item { SectionHeader(stringResource(R.string.notifications)) }
            item {
                MonitorCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.channel_settings), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick = onNotifications) {
                        Text(stringResource(R.string.notification_preferences))
                    }
                    HorizontalDivider()
                    Text(stringResource(R.string.firebase_token), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.firebase_pending), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item { SectionHeader(stringResource(R.string.about)) }
            item {
                MonitorCard(Modifier.fillMaxWidth()) {
                    BrandMark()
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.internal_label), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.version, BuildConfig.VERSION_NAME))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    MonitorTheme { SettingsScreen(ThemeMode.SYSTEM, {}, {}) }
}
