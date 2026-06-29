package com.xnihilfx.sirmobile.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = SirNavy, onPrimary = White,
    secondary = SirTeal, onSecondary = White,
    tertiary = SirTeal, onTertiary = White,
    background = SlateBg, onBackground = SirNavyDeep,
    surface = SlateSurface, onSurface = SirNavyDeep,
    surfaceVariant = SlateMuted, onSurfaceVariant = SlateSubtle,
    outline = SlateBorder, error = SirRed, onError = White,
)

private val DarkColors = darkColorScheme(
    primary = SirTealLight, onPrimary = SirNavyDeep,
    secondary = SirTeal, onSecondary = White,
    background = SirNavyDeep, onBackground = White,
    surface = Color(0xFF16213B), onSurface = White,
    surfaceVariant = Color(0xFF1F2A44), onSurfaceVariant = Color(0xFFB6C2D9),
    outline = Color(0xFF2C3A57), error = SirRed, onError = White,
)

@Composable
fun SirTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(colorScheme = colors, typography = Typography, content = content)
}
