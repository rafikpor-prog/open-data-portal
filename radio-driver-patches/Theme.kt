package pl.radiodriver.app.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF16E0D2),
    secondary = Color(0xFFFFB547),
    background = Color(0xFF061317),
    surface = Color(0xFF0B2026),
    surfaceVariant = Color(0xFF123039),
    onPrimary = Color(0xFF00201E),
    onBackground = Color(0xFFE4F7F4),
    onSurface = Color(0xFFE4F7F4)
)

@Composable
fun RadioDriverTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkScheme, typography = Typography(), content = content)
}
