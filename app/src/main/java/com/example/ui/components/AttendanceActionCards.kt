package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AttendanceRecord
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VtpOrange
import com.example.ui.theme.VtpOrangeDark
import com.example.ui.theme.VtpOrangeLight

@Composable
fun AttendanceActionCards(
    lastTimeIn: AttendanceRecord?,
    lastTimeOut: AttendanceRecord?,
    isProcessingTimeIn: Boolean,
    isProcessingTimeOut: Boolean,
    onTimeInClick: () -> Unit,
    onTimeOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Action Cards: Time In & Time Out side-by-side with Rich Gradients
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TIME IN CARD (Vibrant Sunrise Orange Gradient)
        AttendanceGradientCard(
            title = "Time In",
            subtitle = if (lastTimeIn != null) "Punched Today" else "Start Shift",
            isTimeIn = true,
            markedRecord = lastTimeIn,
            isProcessing = isProcessingTimeIn,
            onClick = onTimeInClick,
            testTag = "time_in_button",
            modifier = Modifier.weight(1f)
        )

        // TIME OUT CARD (Obsidian Black & Charcoal Gradient)
        AttendanceGradientCard(
            title = "Time Out",
            subtitle = if (lastTimeOut != null) "Punched Today" else "End Shift",
            isTimeIn = false,
            markedRecord = lastTimeOut,
            isProcessing = isProcessingTimeOut,
            onClick = onTimeOutClick,
            testTag = "time_out_button",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AttendanceGradientCard(
    title: String,
    subtitle: String,
    isTimeIn: Boolean,
    markedRecord: AttendanceRecord?,
    isProcessing: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    // Gradient styling definitions
    val cardBackgroundGradient = if (isTimeIn) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFFFF7ED),
                Color(0xFFFFEDD5)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF9FAFB),
                Color(0xFFF3F4F6)
            )
        )
    }

    val cardBorderGradient = if (isTimeIn) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFF6600),
                Color(0xFFFFA040),
                Color(0xFFFF6600)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF27272A),
                Color(0xFF52525B),
                Color(0xFF27272A)
            )
        )
    }

    val iconContainerGradient = if (isTimeIn) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFF4500),
                Color(0xFFFF6600),
                Color(0xFFFFA040)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF27272A),
                Color(0xFF18181B),
                Color(0xFF0C0A09)
            )
        )
    }

    val glowShadowColor = if (isTimeIn) Color(0xFFFF6600) else Color(0xFF18181B)
    val iconVector = if (isTimeIn) Icons.Default.Login else Icons.Default.Logout

    Surface(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = glowShadowColor.copy(alpha = 0.25f),
                spotColor = glowShadowColor.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = if (isTimeIn) 1.8.dp else 1.4.dp,
                brush = cardBorderGradient,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isProcessing,
                onClick = onClick
            )
            .testTag(testTag),
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBackgroundGradient)
                .padding(vertical = 22.dp, horizontal = 14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Radiant Icon Aura Ring
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            if (isTimeIn) Color(0xFFFF6600).copy(alpha = 0.12f)
                            else Color(0xFF27272A).copy(alpha = 0.08f)
                        )
                        .border(
                            1.5.dp,
                            if (isTimeIn) Color(0xFFFF6600).copy(alpha = 0.25f)
                            else Color(0xFF27272A).copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = if (isTimeIn) VtpOrange else Color(0xFF18181B),
                            strokeWidth = 3.dp
                        )
                    } else {
                        // Inner Gradient Circle
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(iconContainerGradient)
                                .shadow(4.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = title,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card Action Title
                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.2).sp
                )

                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status Pill / Timestamp with Gradient Accent
                if (markedRecord != null) {
                    val recordTime = markedRecord.formattedDateTime.substringAfter(" ")
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isTimeIn) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFEA580C), Color(0xFFFF6600))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF27272A), Color(0xFF18181B))
                                    )
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = recordTime,
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isTimeIn) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFF6600).copy(alpha = 0.15f), Color(0xFFFF8533).copy(alpha = 0.25f))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF27272A).copy(alpha = 0.10f), Color(0xFF52525B).copy(alpha = 0.18f))
                                    )
                                }
                            )
                            .border(
                                1.dp,
                                if (isTimeIn) Color(0xFFFF6600).copy(alpha = 0.4f)
                                else Color(0xFF52525B).copy(alpha = 0.3f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Tap to Record",
                            fontSize = 11.5.sp,
                            color = if (isTimeIn) VtpOrangeDark else Color(0xFF18181B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
