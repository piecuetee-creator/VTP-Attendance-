package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AttendanceRecord
import com.example.model.AttendanceType
import com.example.model.EmployeeProfile
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ConsoleBackground
import com.example.ui.theme.ConsoleGreen
import com.example.ui.theme.DIBEmeraldDark
import com.example.ui.theme.DIBEmeraldPrimary
import com.example.ui.theme.DIBGoldAccent
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.TimeInGreen
import com.example.ui.theme.TimeInGreenContainer
import com.example.ui.theme.TimeOutAmber
import com.example.ui.theme.TimeOutAmberContainer

@Composable
fun AttendanceSuccessDialog(
    record: AttendanceRecord,
    profile: EmployeeProfile,
    isAlreadyMarked: Boolean = false,
    onDismiss: () -> Unit
) {
    var showPacketDetails by remember { mutableStateOf(false) }
    val isTimeIn = record.type == AttendanceType.TIME_IN
    val primaryColor = if (isTimeIn) TimeInGreen else TimeOutAmber
    val containerColor = if (isTimeIn) TimeInGreenContainer else TimeOutAmberContainer
    val actionTitle = if (isTimeIn) "Time In" else "Time Out"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("attendance_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close button
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BorderSubtle.copy(alpha = 0.5f))
                            .testTag("dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dialog",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Centered Checkmark Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(containerColor)
                        .border(2.dp, primaryColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = primaryColor,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title (e.g. "Time In is already marked!" or "Time In marked successfully!")
                val headline = if (isAlreadyMarked) {
                    "$actionTitle is already marked!"
                } else {
                    "$actionTitle marked successfully!"
                }

                Text(
                    text = headline,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Date and Time (e.g. "07-Sep-2026 09:06 AM")
                Text(
                    text = record.formattedDateTime,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryColor
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Employee Detail Card (Matching Screenshot 2)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceCanvas,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileDetailRow(label = "Employee ID:", value = profile.employeeId)
                        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                        ProfileDetailRow(label = "Name:", value = profile.name)
                        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                        ProfileDetailRow(label = "Designation:", value = profile.designation)
                        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                        ProfileDetailRow(label = "Location:", value = profile.location)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Expandable GT06 Packet Telemetry
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showPacketDetails = !showPacketDetails }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = DIBEmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GT06 Packet Details",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DIBEmeraldPrimary
                        )
                    }
                    Icon(
                        imageVector = if (showPacketDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = DIBEmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(visible = showPacketDetails) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ConsoleBackground)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "GPS: Lat ${String.format("%.4f", record.latitude)}, Lon ${String.format("%.4f", record.longitude)}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = ConsoleGreen
                        )
                        Text(
                            text = "IMEI: ${record.imei}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8)
                        )
                        if (record.txHex != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "TX: ${record.txHex}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // "Ok" Action Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dialog_ok_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DIBEmeraldPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Ok",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
