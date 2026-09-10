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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.VtpOrange
import com.example.ui.theme.VtpOrangeDark

@Composable
fun AttendanceHeader(
    connectionStatus: ConnectionStatus,
    onOpenTerminal: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    onLock: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

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
        color = Color(0xFF090D16),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F1523),
                                Color(0xFF0C121E),
                                Color(0xFF090D16)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top row: VTP Logo/Badge & Three-Dots Menu in Top Right
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
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        1.5.dp,
                                        Brush.linearGradient(
                                            listOf(Color(0xFFFF3D00), Color(0xFFFF6D00), Color(0xFFFF9E44))
                                        ),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .background(Color(0xFF000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.vtp_logo),
                                    contentDescription = "VTP Presence Logo",
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Presence",
                                        color = Color.White,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.2.sp
                                    )
                                    Spacer(modifier = Modifier.width(7.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFFFF3D00), Color(0xFFFF7043))
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
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Right: 3 Dots Overflow Menu button (hiding technical console & settings from regular view)
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF131B2A))
                                    .border(1.dp, Color(0xFF222F48), CircleShape)
                                    .testTag("three_dots_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier
                                    .background(Color(0xFF131B2A))
                                    .border(1.dp, Color(0xFF222F48), RoundedCornerShape(12.dp))
                            ) {
                                if (onOpenSettings != null) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Settings,
                                                    contentDescription = null,
                                                    tint = VtpOrange,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "Settings",
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        },
                                        onClick = {
                                            showMenu = false
                                            onOpenSettings()
                                        },
                                        modifier = Modifier.testTag("menu_settings")
                                    )
                                }

                                if (onOpenTerminal != null) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Terminal,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFF9800),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "Console Logs",
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        },
                                        onClick = {
                                            showMenu = false
                                            onOpenTerminal()
                                        },
                                        modifier = Modifier.testTag("menu_console")
                                    )
                                }

                                if (onLock != null) {
                                    HorizontalDivider(color = Color(0xFF222F48), thickness = 0.5.dp)
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "Switch User / Sign Out",
                                                    color = Color(0xFFFCA5A5),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        },
                                        onClick = {
                                            showMenu = false
                                            onLock()
                                        },
                                        modifier = Modifier.testTag("menu_switch_user")
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hero Identity Bar: "powered by VTP" & Live Attendance Network Status (no GT06 technical wording)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF131B2A),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFF222F48), Color(0xFFFF5722).copy(alpha = 0.35f), Color(0xFF222F48))
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "POWERED BY VTP",
                                    color = Color(0xFFFF7043),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Automated Attendance Server",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }

                            // Attendance Server Status Chip with pulse
                            val (statusText, statusColor) = when (connectionStatus) {
                                ConnectionStatus.CONNECTED, ConnectionStatus.SUCCESS -> "Online" to Color(0xFF10B981)
                                ConnectionStatus.CONNECTING, ConnectionStatus.SENDING_LOGIN, ConnectionStatus.SENDING_LOCATION -> "Connecting..." to Color(0xFFF59E0B)
                                ConnectionStatus.ERROR -> "Offline" to Color(0xFFEF4444)
                                else -> "Standby" to Color(0xFF64748B)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(statusColor.copy(alpha = 0.15f))
                                    .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .scale(if (connectionStatus == ConnectionStatus.CONNECTING) pulseScale else 1f)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = statusText,
                                    color = Color.White,
                                    fontSize = 11.sp,
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
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFF3D00),
                                Color(0xFFFF5722),
                                Color(0xFFFF9800),
                                Color(0xFFFF5722),
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
