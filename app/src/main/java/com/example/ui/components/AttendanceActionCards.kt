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
    timeInCount: Int = 0,
    timeOutCount: Int = 0,
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
            subtitle = when {
                lastTimeIn != null && !lastTimeIn.isSynced -> "In Queue (Offline)"
                timeInCount > 0 -> "Tap for Next IN"
                else -> "Start Shift"
            },
            isTimeIn = true,
            markedRecord = lastTimeIn,
            punchCount = timeInCount,
            isProcessing = isProcessingTimeIn,
            onClick = onTimeInClick,
            testTag = "time_in_button",
            modifier = Modifier.weight(1f)
        )

        // TIME OUT CARD (Obsidian Black & Charcoal Gradient with Crisp Amber Accent)
        AttendanceGradientCard(
            title = "Time Out",
            subtitle = when {
                lastTimeOut != null && !lastTimeOut.isSynced -> "In Queue (Offline)"
                timeOutCount > 0 -> "Tap for Next OUT"
                else -> "End Shift"
            },
            isTimeIn = false,
            markedRecord = lastTimeOut,
            punchCount = timeOutCount,
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
    punchCount: Int,
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

    // Gradient styling definitions: Sleek obsidian glass cards with vibrant orange and slate themes
    val cardBackgroundGradient = if (isTimeIn) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1F1610),
                Color(0xFF171210),
                Color(0xFF10131B)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF161E2E),
                Color(0xFF121926),
                Color(0xFF0F1523)
            )
        )
    }

    val cardBorderGradient = if (isTimeIn) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFF3D00),
                Color(0xFFFF7043),
                Color(0xFFFF3D00)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF475569),
                Color(0xFF64748B),
                Color(0xFF334155)
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
                Color(0xFF334155),
                Color(0xFF1E293B),
                Color(0xFF0F172A)
            )
        )
    }

    val glowShadowColor = if (isTimeIn) Color(0xFFFF6600) else Color(0xFF334155)
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
            // Small IN / OUT daily count badge at top-right
            if (punchCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isTimeIn) VtpOrange.copy(alpha = 0.22f)
                            else Color(0xFFF59E0B).copy(alpha = 0.22f)
                        )
                        .border(
                            1.dp,
                            if (isTimeIn) VtpOrange.copy(alpha = 0.6f)
                            else Color(0xFFF59E0B).copy(alpha = 0.6f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                        .testTag(if (isTimeIn) "time_in_count_badge" else "time_out_count_badge")
                ) {
                    Text(
                        text = if (isTimeIn) "IN: $punchCount" else "OUT: $punchCount",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isTimeIn) Color(0xFFFFB28A) else Color(0xFFFDE68A)
                    )
                }
            }

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
                            else Color(0xFF94A3B8).copy(alpha = 0.12f)
                        )
                        .border(
                            1.5.dp,
                            if (isTimeIn) Color(0xFFFF6600).copy(alpha = 0.25f)
                            else Color(0xFF94A3B8).copy(alpha = 0.25f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = if (isTimeIn) VtpOrange else Color(0xFFF59E0B),
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
                    val isInQueue = !markedRecord.isSynced
                    val displayNum = punchCount.coerceAtLeast(markedRecord.punchNumber)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                when {
                                    isInQueue -> Brush.horizontalGradient(
                                        listOf(Color(0xFFD97706), Color(0xFFF59E0B))
                                    )
                                    isTimeIn -> Brush.horizontalGradient(
                                        listOf(Color(0xFFEA580C), Color(0xFFFF6600))
                                    )
                                    else -> Brush.horizontalGradient(
                                        listOf(Color(0xFF334155), Color(0xFF475569))
                                    )
                                }
                            )
                            .border(
                                width = if (!isTimeIn && !isInQueue) 1.dp else 0.dp,
                                color = if (!isTimeIn && !isInQueue) Color(0xFFF59E0B).copy(alpha = 0.5f) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isInQueue) Icons.Default.Schedule else Icons.Default.Check,
                                contentDescription = if (isInQueue) "In Queue" else "Marked",
                                tint = if (!isTimeIn && !isInQueue) Color(0xFFFBBF24) else Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isInQueue) "Queue #$displayNum • $recordTime" else "#$displayNum • $recordTime",
                                color = Color.White,
                                fontSize = 11.sp,
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
                                        listOf(Color(0xFFFF6600).copy(alpha = 0.18f), Color(0xFFFF8533).copy(alpha = 0.28f))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF334155).copy(alpha = 0.45f), Color(0xFF475569).copy(alpha = 0.45f))
                                    )
                                }
                            )
                            .border(
                                1.dp,
                                if (isTimeIn) Color(0xFFFF6600).copy(alpha = 0.45f)
                                else Color(0xFF64748B).copy(alpha = 0.45f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Tap to Record",
                            fontSize = 11.5.sp,
                            color = if (isTimeIn) Color(0xFFFFAB70) else Color(0xFFE2E8F0),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
