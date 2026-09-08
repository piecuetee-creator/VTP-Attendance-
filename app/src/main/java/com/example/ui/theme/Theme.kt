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
    primary = VtpOrange,
    onPrimary = Color.White,
    primaryContainer = VtpOrangeContainer,
    onPrimaryContainer = VtpOnOrangeContainer,
    secondary = VtpBlack,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF4F4F5),
    onSecondaryContainer = Color(0xFF18181B),
    tertiary = VtpOrangeDark,
    background = SurfaceCanvas,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF4F4F5),
    onSurfaceVariant = TextSecondary,
    outline = BorderMedium,
    outlineVariant = BorderSubtle
)

private val DarkColorScheme = darkColorScheme(
    primary = VtpOrangeLight,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF381804),
    onPrimaryContainer = Color(0xFFFFD4B8),
    secondary = VtpWhite,
    onSecondary = VtpBlack,
    background = VtpBlack,
    onBackground = Color.White,
    surface = VtpDarkSurface,
    onSurface = Color.White,
    surfaceVariant = VtpDarkCard,
    onSurfaceVariant = Color(0xFFA1A1AA),
    outline = VtpDarkBorder,
    outlineVariant = Color(0xFF27272A)
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

