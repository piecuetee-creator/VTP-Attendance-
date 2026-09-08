package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BiometricCyan
import com.example.ui.theme.BiometricCyanContainer
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DIBEmeraldDark
import com.example.ui.theme.DIBEmeraldPrimary
import com.example.ui.theme.DIBGoldAccent
import com.example.ui.theme.DIBGoldContainer
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun AttendanceSecondaryActions(
    onResetBiometric: () -> Unit,
    onOpenConsole: () -> Unit,
    onLockSession: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        label = "buttonScale"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Reset Biometric / Biometric Authentication Button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    1.5.dp,
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(BiometricCyan.copy(alpha = 0.7f), Color(0xFF00E5FF).copy(alpha = 0.3f))
                    ),
                    RoundedCornerShape(16.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onResetBiometric
                )
                .testTag("reset_biometric_button"),
            color = SurfaceCard,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(BiometricCyanContainer, Color(0xFFE0F7FA))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Biometric Icon",
                            tint = BiometricCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Reset Biometric",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Clear today's marked status cache",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = BiometricCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Live GT06 Socket Console Action
        OutlinedButton(
            onClick = onOpenConsole,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("open_live_console_button"),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DIBEmeraldPrimary.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = SurfaceCard,
                contentColor = DIBEmeraldPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Open Live Socket Console (GT06)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (onLockSession != null) {
            OutlinedButton(
                onClick = onLockSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("lock_session_button"),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SurfaceCard,
                    contentColor = TextSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Customer Authentication / Lock Session",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Branding Footer: Presence • Attendance System | powered by VTP
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Presence",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = " • ",
                    fontSize = 12.sp,
                    color = TextTertiary
                )
                Text(
                    text = "Attendance System",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = DIBGoldContainer
            ) {
                Text(
                    text = "powered by VTP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DIBEmeraldDark,
                    letterSpacing = 0.4.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}
