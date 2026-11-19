package id.casan.monitor

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import id.casan.monitor.core.designsystem.theme.MonitorTheme
import id.casan.monitor.core.navigation.MonitorNavigation
import id.casan.monitor.data.local.ThemeMode
import id.casan.monitor.feature.dashboard.MonitoringViewModel
import id.casan.monitor.feature.settings.SettingsViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val monitoring: MonitoringViewModel by viewModels()
    private val settings: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by monitoring.state.collectAsStateWithLifecycle()
            val search by monitoring.alertSearch.collectAsStateWithLifecycle()
            val theme by settings.theme.collectAsStateWithLifecycle()
            val dark = when (theme) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            MonitorTheme(dark) {
                MonitorNavigation(
                    state = state,
                    search = search,
                    onQuery = monitoring::updateAlertQuery,
                    onSeverity = monitoring::updateAlertSeverity,
                    mode = theme,
                    onTheme = settings::setTheme,
                    onNotifications = ::openNotifications,
                    retry = monitoring::reload,
                )
            }
        }
    }

    private fun openNotifications() {
        startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
    }
}
