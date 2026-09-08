package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.network.ConnectionStatus
import com.example.ui.theme.VtpAccentGold
import com.example.ui.theme.VtpAccentGoldLight
import com.example.ui.theme.VtpPrimary
import com.example.ui.theme.VtpPrimaryDark

@Composable
fun AttendanceHeader(
    connectionStatus: ConnectionStatus,
    onOpenTerminal: () -> Unit,
    onOpenSettings: () -> Unit,
    onLock: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF12100E),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF221D18),
                                Color(0xFF17130F),
                                Color(0xFF0D0B0A)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top row: VTP Logo/Badge & Navigation Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: VTP Brand Emblem & Attendance Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .border(
                                        1.8.dp,
                                        Brush.linearGradient(
                                            listOf(Color(0xFFFF4500), Color(0xFFFF7A00), Color(0xFFFFA040))
                                        ),
                                        RoundedCornerShape(13.dp)
                                    )
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_vtp_presence_logo),
                                    contentDescription = "VTP Presence Attendance Logo",
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(13.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Presence",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.2.sp
                                    )
                                    Spacer(modifier = Modifier.width(7.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFFFF5722), Color(0xFFFF9800))
                                                )
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "VTP",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                                Text(
                                    text = "Smart Attendance System",
                                    color = Color(0xFFB0A8A0),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Right: Live Terminal and Settings actions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onOpenTerminal,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                                    .testTag("console_logs_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = "Live GT06 Console",
                                    tint = Color(0xFFFFA040),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = onOpenSettings,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                                    .testTag("settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            if (onLock != null) {
                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = onLock,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                                        .testTag("lock_auth_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Lock Session",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hero Identity Bar: "powered by VTP" & Live GT06 Socket Status with subtle glass gradient
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Black.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Brush.horizontalGradient(
                                listOf(Color.White.copy(alpha = 0.12f), Color(0xFFFF7A00).copy(alpha = 0.3f), Color.White.copy(alpha = 0.12f))
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "POWERED BY VTP",
                                        color = Color(0xFFFF9E44),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                                Text(
                                    text = "GT06 Binary Protocol • Port 5200",
                                    color = Color(0xFFD4C8BE),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }

                            // Socket Status Chip with pulse
                            val (statusText, statusColor) = when (connectionStatus) {
                                ConnectionStatus.CONNECTED, ConnectionStatus.SUCCESS -> "Online" to Color(0xFF34D399)
                                ConnectionStatus.CONNECTING, ConnectionStatus.SENDING_LOGIN, ConnectionStatus.SENDING_LOCATION -> "Syncing..." to Color(0xFFFBBF24)
                                ConnectionStatus.ERROR -> "Offline" to Color(0xFFF87171)
                                else -> "Standby" to Color(0xFFA1A1AA)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(statusColor.copy(alpha = 0.15f), statusColor.copy(alpha = 0.25f))
                                        )
                                    )
                                    .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 11.dp, vertical = 5.dp)
                                    .clickable { onOpenTerminal() }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .scale(if (connectionStatus == ConnectionStatus.CONNECTING) pulseScale else 1f)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = statusText,
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Luminous Accent Gradient Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFF3D00),
                                Color(0xFFFF7A00),
                                Color(0xFFFFA726),
                                Color(0xFFFF7A00),
                                Color(0xFFFF3D00)
                            )
                        )
                    )
            )
        }
    }
}

// Backward-compatibility alias
@Composable
fun DIBHeader(
    connectionStatus: ConnectionStatus,
    onOpenTerminal: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    AttendanceHeader(
        connectionStatus = connectionStatus,
        onOpenTerminal = onOpenTerminal,
        onOpenSettings = onOpenSettings,
        modifier = modifier
    )
}
