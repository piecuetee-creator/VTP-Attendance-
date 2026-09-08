package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// VTP Attendance Palette: Orange, Black, White
val VtpOrange = Color(0xFFFF6600)
val VtpOrangeDark = Color(0xFFD45200)
val VtpOrangeLight = Color(0xFFFF8533)
val VtpOrangeContainer = Color(0xFFFFECE0)
val VtpOnOrangeContainer = Color(0xFF431800)

val VtpBlack = Color(0xFF0C0C0D)
val VtpDarkSurface = Color(0xFF161618)
val VtpDarkCard = Color(0xFF1F1F23)
val VtpDarkBorder = Color(0xFF2E2E33)

val VtpWhite = Color(0xFFFFFFFF)
val VtpOffWhite = Color(0xFFF7F7F8)
val VtpLightBorder = Color(0xFFE5E7EB)

// Gradients for Modern Visual Appeal
val VtpOrangeGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFF4500), Color(0xFFFF6600), Color(0xFFFFA040))
)

val VtpSunriseGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFEA580C), Color(0xFFFF7A00), Color(0xFFFBBF24))
)

val VtpWarmDarkGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF1C1917), Color(0xFF141211), Color(0xFF0C0A09))
)

val VtpHeaderGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1A1715), Color(0xFF12100E), Color(0xFF0A0908))
)

val VtpCardGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFFBF7), Color(0xFFFFF5EB))
)

val VtpTimeInGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFEA580C), Color(0xFFFF6600), Color(0xFFFF8533))
)

val VtpTimeOutGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF262626), Color(0xFF18181B), Color(0xFF0F0F10))
)

val VtpGlowBorderGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFF6600), Color(0xFFFBBF24), Color(0xFFFF6600))
)

// Primary brand colors
val VtpPrimary = VtpOrange
val VtpPrimaryDark = VtpOrangeDark
val VtpPrimaryLight = VtpOrangeLight
val VtpPrimaryContainer = VtpOrangeContainer
val VtpOnPrimaryContainer = VtpOnOrangeContainer

val VtpAccentGold = Color(0xFFFF8533)
val VtpAccentGoldLight = Color(0xFFFFAB66)
val VtpAccentGoldContainer = Color(0xFFFFECE0)

// Backward compatibility aliases
val DIBEmeraldPrimary = VtpPrimary
val DIBEmeraldDark = VtpPrimaryDark
val DIBEmeraldLight = VtpPrimaryLight
val DIBEmeraldContainer = VtpPrimaryContainer
val DIBOnEmeraldContainer = VtpOnPrimaryContainer
val DIBGoldAccent = VtpAccentGold
val DIBGoldLight = VtpAccentGoldLight
val DIBGoldContainer = VtpAccentGoldContainer

val SurfaceCanvas = Color(0xFFF9FAFB)
val SurfaceCard = Color(0xFFFFFFFF)
val SurfaceCardElevated = Color(0xFFFFFFFF)

// Action Cards: High contrast Orange & Dark Charcoal / Black
val TimeInGreen = VtpOrange
val TimeInGreenContainer = Color(0xFFFFECE0)
val TimeInGreenBorder = Color(0xFFFF8533)

val TimeOutAmber = Color(0xFF27272A)
val TimeOutAmberContainer = Color(0xFFF4F4F5)
val TimeOutAmberBorder = Color(0xFF52525B)

val BiometricCyan = VtpOrange
val BiometricCyanContainer = Color(0xFFFFECE0)

val TextPrimary = Color(0xFF111827)
val TextSecondary = Color(0xFF4B5563)
val TextTertiary = Color(0xFF9CA3AF)

val BorderSubtle = Color(0xFFE5E7EB)
val BorderMedium = Color(0xFFD1D5DB)

val ConsoleBackground = Color(0xFF0F0F10)
val ConsoleGreen = Color(0xFFFF8533)
val ConsoleCyan = Color(0xFFFFA366)

