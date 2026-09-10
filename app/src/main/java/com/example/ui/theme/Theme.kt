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

private val DarkColorScheme = darkColorScheme(
    primary = VtpOrange,
    onPrimary = Color.White,
    primaryContainer = VtpOrangeContainer,
    onPrimaryContainer = VtpOnOrangeContainer,
    secondary = VtpWhite,
    onSecondary = VtpBlack,
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFF1F5F9),
    background = VtpBlack,
    onBackground = Color.White,
    surface = VtpDarkSurface,
    onSurface = Color.White,
    surfaceVariant = VtpDarkCard,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = VtpDarkBorder,
    outlineVariant = Color(0xFF1E293B)
)

private val LightColorScheme = DarkColorScheme // Standardize on professional dark corporate theme as shown in user screenshot

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

