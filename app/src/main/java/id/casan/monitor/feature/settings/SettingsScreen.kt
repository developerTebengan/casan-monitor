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
import id.casan.monitor.notification.RegistrationStatus

data class SettingsAccount(
    val profile: id.casan.monitor.domain.model.MonitorAccount,
    val notificationsEnabled: Boolean,
    val mayRequestPermission: Boolean,
    val registration: RegistrationStatus,
    val onRetryRegistration: () -> Unit,
    val onRequestPermission: () -> Unit,
    val onLogout: () -> Unit,
)

@Composable
fun SettingsScreen(
    mode: ThemeMode,
    onTheme: (ThemeMode) -> Unit,
    onNotifications: () -> Unit,
    account: SettingsAccount? = null,
) {
    PageFrame { modifier ->
        LazyColumn(
            modifier,
            contentPadding = PaddingValues(Space.medium),
            verticalArrangement = Arrangement.spacedBy(Space.medium),
        ) {
            item { Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium) }
            if (account != null) item {
                SectionHeader("Account")
                MonitorCard(Modifier.fillMaxWidth()) {
                    if (account.profile.name.isNotBlank()) Text(account.profile.name)
                    if (account.profile.phone.isNotBlank()) Text(account.profile.phone)
                    if (account.profile.email.isNotBlank()) Text(account.profile.email)
                    OutlinedButton(onClick = account.onLogout) { Text("Sign out") }
                }
            }
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
                    if (account != null) {
                        val permission = when {
                            account.notificationsEnabled -> "Notifications enabled"
                            account.mayRequestPermission -> "Permission required"
                            else -> "Notifications disabled"
                        }
                        Text(permission)
                        if (account.mayRequestPermission) {
                            Text("Allow notifications to receive CASAN alerts when the app is in the background.")
                            Button(onClick = account.onRequestPermission) { Text("Allow notifications") }
                        }
                        Text(when (account.registration) {
                            RegistrationStatus.WAITING -> "Waiting to register this device"
                            RegistrationStatus.OFFLINE -> "Waiting for an internet connection"
                            RegistrationStatus.REGISTERING -> "Registering this device"
                            RegistrationStatus.REGISTERED -> "Device registered"
                            RegistrationStatus.FIREBASE_UNAVAILABLE -> "Firebase is not configured for this build"
                            RegistrationStatus.FAILED -> "Device registration failed"
                        })
                        if (account.registration == RegistrationStatus.FAILED) {
                            TextButton(onClick = account.onRetryRegistration) { Text("Retry registration") }
                        }
                    }
                }
            }
            item { SectionHeader(stringResource(R.string.about)) }
            item {
                MonitorCard(Modifier.fillMaxWidth()) {
                    BrandMark()
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.internal_label), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.version, BuildConfig.VERSION_NAME))
                    if (BuildConfig.DEBUG) {
                        Text("Backend " + BuildConfig.BACKEND_URL.ifBlank { "unconfigured" })
                    }
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
