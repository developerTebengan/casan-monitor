package id.casan.monitor.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val Light = lightColorScheme(
    primary = Brand, onPrimary = Ink, primaryContainer = Color(0xFFDDF5EE),
    onPrimaryContainer = Ink, secondaryContainer = Brand, onSecondaryContainer = Ink,
    background = Color(0xFFF7F9FA), onBackground = Ink,
    surface = Color.White, onSurface = Ink, surfaceVariant = Color(0xFFEDF4F2),
    onSurfaceVariant = Color(0xFF52645F), outline = Color(0xFF71817D),
    outlineVariant = Color(0xFFE5ECE9), error = Color(0xFFB42323),
)

private val Dark = darkColorScheme(
    primary = Brand, onPrimary = Ink, primaryContainer = Color(0xFF203A34),
    onPrimaryContainer = Color(0xFFF1F7F5), secondaryContainer = Brand, onSecondaryContainer = Ink,
    background = Color(0xFF101817), onBackground = Color(0xFFF1F7F5),
    surface = Color(0xFF192321), onSurface = Color(0xFFF1F7F5), surfaceVariant = Color(0xFF24312E),
    onSurfaceVariant = Color(0xFFA3B5B0), outline = Color(0xFFA3B5B0),
    outlineVariant = Color(0xFF2C3A36), error = Color(0xFFFFB4AB),
)

object Space {
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val control = 48.dp
}

@Composable
fun MonitorTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) Dark else Light,
        typography = MonitorTypography,
        content = content,
    )
}
