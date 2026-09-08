package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = DIBEmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = DIBEmeraldContainer,
    onPrimaryContainer = DIBOnEmeraldContainer,
    secondary = DIBGoldAccent,
    onSecondary = Color.White,
    secondaryContainer = DIBGoldContainer,
    onSecondaryContainer = Color(0xFF281E00),
    tertiary = BiometricCyan,
    background = SurfaceCanvas,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF0F5F2),
    onSurfaceVariant = TextSecondary,
    outline = BorderMedium,
    outlineVariant = BorderSubtle
)

private val DarkColorScheme = darkColorScheme(
    primary = DIBEmeraldLight,
    onPrimary = Color.White,
    primaryContainer = DIBEmeraldDark,
    onPrimaryContainer = DIBEmeraldContainer,
    secondary = DIBGoldLight,
    onSecondary = Color(0xFF281E00),
    background = Color(0xFF0D1713),
    onBackground = Color(0xFFE3EDE8),
    surface = Color(0xFF14241E),
    onSurface = Color(0xFFE3EDE8),
    surfaceVariant = Color(0xFF1B2F27),
    onSurfaceVariant = Color(0xFFA6BCB2)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun vtpTextFieldColors() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedBorderColor = DIBEmeraldPrimary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = DIBEmeraldPrimary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
    focusedLeadingIconColor = DIBEmeraldPrimary,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = DIBEmeraldPrimary
)

