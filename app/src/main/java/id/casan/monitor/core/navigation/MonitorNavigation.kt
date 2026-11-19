package id.casan.monitor.core.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.casan.monitor.R
import id.casan.monitor.core.designsystem.components.MonitorIcon
import id.casan.monitor.core.designsystem.components.MonitorTopAppBar
import id.casan.monitor.data.local.ThemeMode
import id.casan.monitor.domain.model.IncidentSeverity
import id.casan.monitor.feature.alerts.AlertsScreen
import id.casan.monitor.feature.dashboard.AlertSearchState
import id.casan.monitor.feature.dashboard.DashboardScreen
import id.casan.monitor.feature.dashboard.MonitoringState
import id.casan.monitor.feature.history.HistoryScreen
import id.casan.monitor.feature.settings.SettingsScreen

private enum class Destination(val route: String, val label: Int, val icon: Int) {
    DASHBOARD("dashboard", R.string.dashboard, R.drawable.ic_dashboard),
    ALERTS("alerts", R.string.alerts, R.drawable.ic_bell),
    HISTORY("history", R.string.history, R.drawable.ic_history),
    SETTINGS("settings", R.string.settings, R.drawable.ic_settings),
}

@Composable
fun MonitorNavigation(
    state: MonitoringState,
    search: AlertSearchState,
    onQuery: (String) -> Unit,
    onSeverity: (IncidentSeverity?) -> Unit,
    mode: ThemeMode,
    onTheme: (ThemeMode) -> Unit,
    onNotifications: () -> Unit,
    retry: () -> Unit,
) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val activeRoute = entry?.destination?.route ?: Destination.DASHBOARD.route

    fun select(destination: Destination) {
        if (nav.currentDestination?.route == destination.route) return
        nav.navigate(destination.route) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = { MonitorTopAppBar({ select(Destination.ALERTS) }, { select(Destination.SETTINGS) }) },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Destination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = activeRoute == destination.route,
                        onClick = { select(destination) },
                        icon = { MonitorIcon(destination.icon) },
                        label = { Text(stringResource(destination.label)) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primary),
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Destination.DASHBOARD.route,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding).imePadding(),
        ) {
            composable(Destination.DASHBOARD.route) { DashboardScreen(state, { select(Destination.ALERTS) }, retry) }
            composable(Destination.ALERTS.route) { AlertsScreen(state, search, onQuery, onSeverity, retry) }
            composable(Destination.HISTORY.route) { HistoryScreen(state, retry) }
            composable(Destination.SETTINGS.route) { SettingsScreen(mode, onTheme, onNotifications) }
        }
    }
}
