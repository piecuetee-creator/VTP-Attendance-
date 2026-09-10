package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// VTP Attendance Palette: High-End Obsidian Dark (#0A0E17 / #111928), Vibrant Electric Orange (#FF5722 / #FF6D00), Crisp Ice White
val VtpOrange = Color(0xFFFF5722)
val VtpOrangeDark = Color(0xFFE64A19)
val VtpOrangeLight = Color(0xFFFF7043)
val VtpOrangeContainer = Color(0xFF2A150A)
val VtpOnOrangeContainer = Color(0xFFFFCCBC)

val VtpBlack = Color(0xFF090D16)
val VtpDarkSurface = Color(0xFF0F1523)
val VtpDarkCard = Color(0xFF151D2E)
val VtpDarkBorder = Color(0xFF222F48)

val VtpWhite = Color(0xFFFFFFFF)
val VtpOffWhite = Color(0xFFF1F5F9)
val VtpLightBorder = Color(0xFF222F48)

// Gradients for Modern Visual Appeal
val VtpOrangeGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFF3D00), Color(0xFFFF5722), Color(0xFFFF8A65))
)

val VtpSunriseGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFE64A19), Color(0xFFFF5722), Color(0xFFFF9800))
)

val VtpWarmDarkGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF151D2E), Color(0xFF0F1523), Color(0xFF090D16))
)

val VtpHeaderGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0F1523), Color(0xFF0C121E), Color(0xFF090D16))
)

val VtpCardGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF1A2336), Color(0xFF141C2B), Color(0xFF101724))
)

val VtpTimeInGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFE64A19), Color(0xFFFF5722), Color(0xFFFF7043))
)

val VtpTimeOutGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF1E293B), Color(0xFF151E2E), Color(0xFF0F1724))
)

val VtpGlowBorderGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFF5722), Color(0xFFFF9800), Color(0xFFFF5722))
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

val SurfaceCanvas = Color(0xFF090D16)
val SurfaceCard = Color(0xFF131B2A)
val SurfaceCardElevated = Color(0xFF192337)

// Action Cards: High contrast Orange & Deep Slate
val TimeInGreen = VtpOrange
val TimeInGreenContainer = Color(0xFF2A150A)
val TimeInGreenBorder = Color(0xFFFF7043)

val TimeOutAmber = Color(0xFF1E293B)
val TimeOutAmberContainer = Color(0xFF131B2A)
val TimeOutAmberBorder = Color(0xFF334155)

val BiometricCyan = VtpOrange
val BiometricCyanContainer = Color(0xFF2A150A)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF94A3B8)
val TextTertiary = Color(0xFF64748B)

val BorderSubtle = Color(0xFF1E293B)
val BorderMedium = Color(0xFF334155)

val ConsoleBackground = Color(0xFF090D16)
val ConsoleGreen = Color(0xFFFF7043)
val ConsoleCyan = Color(0xFF38BDF8)

