package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FlappyLightColorScheme = lightColorScheme(
    primary = Color(0xFF00ACC1),
    onPrimary = Color.White,
    secondary = FlappySunnyYellow,
    onSecondary = Color(0xFF2E1C00),
    tertiary = FlappyGrassGreen,
    onTertiary = Color.White,
    background = FlappySkyLight,
    onBackground = LightTextPrimary,
    surface = Color.White,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceCard,
    onSurfaceVariant = LightTextSecondary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = FlappyLightColorScheme,
        typography = Typography,
        content = content
    )
}
