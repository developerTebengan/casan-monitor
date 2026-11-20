package id.casan.monitor.core.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import id.casan.monitor.feature.alerts.AlertsScreen
import id.casan.monitor.feature.dashboard.DashboardScreen
import id.casan.monitor.feature.history.HistoryScreen
import id.casan.monitor.feature.notifications.NotificationDetailScreen
import id.casan.monitor.feature.notifications.NotificationState
import id.casan.monitor.notification.isNotificationId
import id.casan.monitor.feature.settings.SettingsAccount
import id.casan.monitor.feature.settings.SettingsScreen

private enum class Destination(val route: String, val label: Int, val icon: Int) {
    DASHBOARD("dashboard", R.string.dashboard, R.drawable.ic_dashboard),
    ALERTS("alerts", R.string.alerts, R.drawable.ic_bell),
    HISTORY("history", R.string.history, R.drawable.ic_history),
    SETTINGS("settings", R.string.settings, R.drawable.ic_settings),
}

@Composable
fun MonitorNavigation(
    state: NotificationState,
    pendingNotification: String?,
    onNotificationOpened: () -> Unit,
    onRead: (String) -> Unit,
    readErrorId: String?,
    mode: ThemeMode,
    onTheme: (ThemeMode) -> Unit,
    onNotifications: () -> Unit,
    retry: () -> Unit,
    account: SettingsAccount,
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

    fun openNotification(id: String) {
        if (isNotificationId(id)) {
            nav.navigate("notification/$id") { launchSingleTop = true }
        }
    }

    LaunchedEffect(pendingNotification) {
        val id = pendingNotification ?: return@LaunchedEffect
        select(Destination.ALERTS)
        openNotification(id)
        onNotificationOpened()
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            if (activeRoute != "notification/{id}") {
                MonitorTopAppBar({ select(Destination.ALERTS) }, { select(Destination.SETTINGS) })
            }
        },
        bottomBar = {
            if (activeRoute != "notification/{id}") NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
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
            composable(Destination.DASHBOARD.route) { DashboardScreen(state, { select(Destination.ALERTS) }, retry, ::openNotification) }
            composable(Destination.ALERTS.route) { AlertsScreen(state, retry, ::openNotification) }
            composable(Destination.HISTORY.route) { HistoryScreen(state, retry, ::openNotification) }
            composable("notification/{id}") { detail ->
                val id = detail.arguments?.getString("id").orEmpty()
                NotificationDetailScreen(state, id, readErrorId == id, { onRead(id) }, retry) {
                    nav.popBackStack()
                }
            }
            composable(Destination.SETTINGS.route) { SettingsScreen(mode, onTheme, onNotifications, account) }
        }
    }
}
