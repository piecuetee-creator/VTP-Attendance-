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
import com.example.ui.theme.VtpOrange

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
    val containerColor = primaryColor.copy(alpha = 0.16f)
    val actionTitle = if (isTimeIn) "Time In" else "Time Out"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("attendance_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1523)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222F48)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
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
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            .testTag("dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dialog",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        .border(2.dp, primaryColor.copy(alpha = 0.5f), CircleShape),
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
                    color = MaterialTheme.colorScheme.onSurface,
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

                // Clean Employee Detail Card: Name, Designation, Location Description
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF151D2E),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFF222F48)
                    )
                ) {
                    val dividerColor = Color(0xFF222F48)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val displayName = if (profile.name.isNotBlank()) profile.name else "Employee ${profile.employeeCode.ifBlank { profile.employeeId }}"
                        ProfileDetailRow(label = "Employee Name:", value = displayName)
                        HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                        val displayCode = profile.employeeCode.ifBlank { profile.employeeId.ifBlank { "000001" } }
                        ProfileDetailRow(label = "Employee Code:", value = displayCode)
                        HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                        val displayCompany = profile.companyCode.ifBlank { "1001" }
                        ProfileDetailRow(label = "Company Code:", value = displayCompany)
                        HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                        // Description of location, NOT latitude/longitude coordinates
                        val rawLoc = record.locationName.trim()
                        val isCoordinateString = rawLoc.startsWith("Lat", ignoreCase = true) ||
                                rawLoc.matches(Regex("^-?\\d+(\\.\\d+)?,\\s*-?\\d+(\\.\\d+)?$"))
                        val resolvedLocation = when {
                            rawLoc.isNotBlank() && !isCoordinateString -> rawLoc
                            profile.location.isNotBlank() && !profile.location.startsWith("Lat", ignoreCase = true) -> profile.location
                            else -> "Headquarters Office"
                        }
                        ProfileDetailRow(label = "Location:", value = resolvedLocation)
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
                        containerColor = VtpOrange,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
