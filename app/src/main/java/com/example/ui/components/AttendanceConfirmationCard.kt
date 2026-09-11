package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AttendanceRecord
import com.example.model.AttendanceType
import com.example.model.EmployeeProfile
import com.example.ui.theme.TimeInGreen
import com.example.ui.theme.TimeOutAmber
import com.example.ui.theme.VtpOrange

@Composable
fun AttendanceConfirmationCard(
    record: AttendanceRecord?,
    profile: EmployeeProfile,
    currentLocationName: String,
    isSyncing: Boolean,
    modifier: Modifier = Modifier
) {
    val isMarked = record != null
    val isTimeIn = record?.type == AttendanceType.TIME_IN
    val primaryColor = if (isTimeIn) TimeInGreen else if (isMarked) TimeOutAmber else VtpOrange
    val cardBorderColor = if (isMarked) primaryColor.copy(alpha = 0.45f) else Color(0xFF222F48)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("attendance_confirmation_card"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF131B2A),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isMarked && record != null) {
                // Confirmation Header
                val actionName = if (isTimeIn) "Time In" else "Time Out"
                val badgeContainerColor = primaryColor.copy(alpha = 0.18f)

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(badgeContainerColor)
                        .border(1.5.dp, primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confirmed",
                        tint = primaryColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "$actionName marked successfully!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = record.formattedDateTime,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VtpOrange,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Background Server Sync indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.8.dp,
                            color = VtpOrange
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Syncing with server in background...",
                            fontSize = 11.sp,
                            color = VtpOrange
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Synced",
                            tint = TimeInGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Synced with Server",
                            fontSize = 11.sp,
                            color = TimeInGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Ready / Pending status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(VtpOrange.copy(alpha = 0.15f))
                            .border(1.dp, VtpOrange.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = VtpOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Ready for Attendance",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Verify biometric with Time In or Time Out",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Details Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F1523),
                border = BorderStroke(1.dp, Color(0xFF1E283D))
            ) {
                val dividerColor = Color(0xFF1E283D)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val displayName = if (profile.name.isNotBlank()) profile.name else "Employee ${profile.employeeCode.ifBlank { profile.employeeId }}"
                    ConfirmationRow(label = "Employee Name:", value = displayName)
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    val displayCode = profile.employeeCode.ifBlank { profile.employeeId.ifBlank { "000001" } }
                    ConfirmationRow(label = "Employee Code:", value = displayCode)
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    val displayCompany = profile.companyCode.ifBlank { "1001" }
                    ConfirmationRow(label = "Company Code:", value = displayCompany)
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    val rawLoc = (record?.locationName ?: currentLocationName.ifBlank { profile.location }).trim()
                    val isCoordinateString = rawLoc.startsWith("Lat", ignoreCase = true) ||
                            rawLoc.matches(Regex("^-?\\d+(\\.\\d+)?,\\s*-?\\d+(\\.\\d+)?$"))
                    val resolvedLocation = when {
                        rawLoc.isNotBlank() && !isCoordinateString -> rawLoc
                        profile.location.isNotBlank() && !profile.location.startsWith("Lat", ignoreCase = true) -> profile.location
                        else -> "Karim Chamber Offices, Karachi"
                    }
                    ConfirmationRow(label = "Location:", value = resolvedLocation)
                }
            }
        }
    }
}

@Composable
private fun ConfirmationRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(0.65f)
        )
    }
}
