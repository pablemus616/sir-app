package com.xnihilfx.sirmobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Blue600,
    onPrimary = White,
    background = Slate50,
    surface = White,
    onSurface = Slate900,
)

@Composable
fun SirTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = LightColors, typography = Typography, content = content)
