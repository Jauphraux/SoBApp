package com.example.shadowsofbrimstonecompanion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Theme Colors
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF8B4513),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDEB887),
    onPrimaryContainer = Color(0xFF3E2723),
    secondary = Color(0xFF707070),
    onSecondary = Color.White,
    background = Color(0xFFF8F8F8),
    surface = Color.White,
    error = Color(0xFFC62828),
    onError = Color.White
)

// Dark Theme Colors
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFDEB887),
    onPrimary = Color(0xFF3E2723),
    primaryContainer = Color(0xFF5D4037),
    onPrimaryContainer = Color(0xFFDEB887),
    secondary = Color(0xFFB0B0B0),
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    surface = Color(0xFF2A2A2A),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF7F0000)
)

@Composable
fun BrimstoneTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}