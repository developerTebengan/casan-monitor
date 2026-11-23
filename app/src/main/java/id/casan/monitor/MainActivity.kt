package id.casan.monitor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.net.ConnectivityManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import id.casan.monitor.core.designsystem.theme.MonitorTheme
import id.casan.monitor.core.navigation.MonitorNavigation
import id.casan.monitor.core.network.validatedConnectivity
import id.casan.monitor.data.repository.AuthError
import id.casan.monitor.data.local.ThemeMode
import id.casan.monitor.data.repository.AuthState
import id.casan.monitor.feature.auth.LoginScreen
import id.casan.monitor.feature.auth.LoginViewModel
import id.casan.monitor.feature.auth.message
import id.casan.monitor.feature.notifications.NotificationCenterViewModel
import id.casan.monitor.feature.notifications.NotificationState
import id.casan.monitor.feature.settings.SettingsAccount
import id.casan.monitor.notification.MonitorDeviceRegistration
import id.casan.monitor.notification.NOTIFICATION_SCHEME
import id.casan.monitor.notification.isQueueNotification
import kotlinx.coroutines.launch
import javax.inject.Inject
import id.casan.monitor.feature.settings.SettingsViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var registration: MonitorDeviceRegistration
    private val auth: LoginViewModel by viewModels()
    private val online = mutableStateOf(true)
    private val notificationsEnabled = mutableStateOf(false)
    private val canRequestNotifications = mutableStateOf(false)
    private val pendingNotification = mutableStateOf<String?>(null)
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        updatePermissionStatus()
    }

    private val notifications: NotificationCenterViewModel by viewModels()
    private val settings: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingNotification.value = if (savedInstanceState == null) {
            notificationFrom(intent)
        } else {
            savedInstanceState.getString("pendingNotification")
        }
        updatePermissionStatus()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                getSystemService(ConnectivityManager::class.java).validatedConnectivity().collect { connected ->
                    online.value = connected
                    if (connected) {
                        notifications.refresh()
                        val state = auth.session.value
                        if (state is AuthState.Unavailable && state.error.isTransientConnectionFailure()) {
                            auth.restore()
                        }
                    }
                }
            }
        }
        setContent {
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
            val session by auth.session.collectAsStateWithLifecycle()
            val loginUi by auth.ui.collectAsStateWithLifecycle()
            val navigationState = rememberSaveableStateHolder()
            var lastAccountId by rememberSaveable { mutableStateOf<Long?>(null) }
            LaunchedEffect(session) {
                when (val current = session) {
                    is AuthState.SignedIn -> {
                        lastAccountId?.takeIf { it != current.account.id }?.let(navigationState::removeState)
                        lastAccountId = current.account.id
                    }
                    AuthState.SignedOut -> {
                        lastAccountId?.let(navigationState::removeState)
                        lastAccountId = null
                    }
                    else -> Unit
                }
            }
            MonitorTheme(dark) {
                when (val current = session) {
                    AuthState.Restoring -> SessionStatus()
                    AuthState.SignedOut -> LoginScreen(loginUi, auth::login)
                    is AuthState.Unavailable -> SessionStatus(
                        current.error.message(),
                        auth::restore,
                        auth::logout,
                    )
                    is AuthState.SignedIn -> navigationState.SaveableStateProvider(current.account.id) {
                        val notificationFlow = remember(current.account.id) { notifications.observe(current.account.id) }
                        val state by notificationFlow.collectAsStateWithLifecycle(initialValue = NotificationState.Loading)
                        val readError by notifications.readError.collectAsStateWithLifecycle()
                        val refreshing by notifications.refreshing.collectAsStateWithLifecycle()
                        val registrationStatus by registration.status.collectAsStateWithLifecycle()
                        MonitorNavigation(
                            state = state,
                            refreshing = refreshing,
                            offline = !online.value,
                            observeDetail = { notifications.observeDetail(current.account.id, it) },
                            pendingNotification = pendingNotification.value,
                            onNotificationOpened = { pendingNotification.value = null },
                            onRead = { notifications.markRead(current.account.id, it) },
                            readErrorId = readError?.takeIf { it.first == current.account.id }?.second,
                            mode = theme,
                            onTheme = settings::setTheme,
                            onNotifications = ::openNotifications,
                            retry = { notifications.refresh(showProgress = true) },
                            account = SettingsAccount(
                                current.account,
                                notificationsEnabled.value,
                                canRequestNotifications.value,
                                registrationStatus,
                                registration::requestSync,
                                ::requestNotifications,
                                auth::logout,
                            ),
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        notifications.refresh()
        val current = auth.session.value as? AuthState.SignedIn
        if (current != null && current.expiresAt <= System.currentTimeMillis()) auth.restore()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationFrom(intent)?.let { pendingNotification.value = it }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("pendingNotification", pendingNotification.value)
        super.onSaveInstanceState(outState)
    }

    private fun AuthError.isTransientConnectionFailure(): Boolean =
        this == AuthError.NETWORK || this == AuthError.TIMEOUT || this == AuthError.SERVER

    private fun notificationFrom(intent: Intent): String? = when {
        intent.action == ACTION_OPEN_NOTIFICATION ->
            intent.data?.takeIf { it.scheme == NOTIFICATION_SCHEME }?.schemeSpecificPart.orEmpty()
        isQueueNotification(intent.getStringExtra("type")) -> ""
        else -> null
    }

    private fun updatePermissionStatus() {
        notificationsEnabled.value = NotificationManagerCompat.from(this).areNotificationsEnabled()
        canRequestNotifications.value = Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
            !getPreferences(MODE_PRIVATE).getBoolean("notification_permission_requested", false)
    }

    private fun requestNotifications() {
        if (!canRequestNotifications.value) return
        getPreferences(MODE_PRIVATE).edit().putBoolean("notification_permission_requested", true).apply()
        canRequestNotifications.value = false
        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    companion object {
        const val ACTION_OPEN_NOTIFICATION = "id.casan.monitor.OPEN_NOTIFICATION"
    }

    private fun openNotifications() {
        startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
    }
}

@Composable
private fun SessionStatus(
    error: String? = null,
    retry: () -> Unit = {},
    signOut: () -> Unit = {},
) {
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.safeDrawingPadding().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (error == null) {
                CircularProgressIndicator()
            } else {
                Text(error)
                Button(onClick = retry) { Text("Retry") }
                TextButton(onClick = signOut) { Text("Sign out") }
            }
        }
    }
}
