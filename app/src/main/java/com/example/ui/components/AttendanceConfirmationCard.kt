package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
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
import androidx.compose.ui.text.style.TextOverflow
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
    modifier: Modifier = Modifier
) {
    val isMarked = record != null
    val isTimeIn = record?.type == AttendanceType.TIME_IN
    val primaryColor = if (isTimeIn) TimeInGreen else if (isMarked) TimeOutAmber else VtpOrange
    val cardBorderColor = if (isMarked) primaryColor.copy(alpha = 0.45f) else Color(0xFF222F48)

    // Data resolution
    val displayName = if (profile.name.isNotBlank()) profile.name else "Employee ${profile.employeeCode.ifBlank { profile.employeeId }}"
    val displayCode = profile.employeeCode.ifBlank { profile.employeeId.ifBlank { "000452" } }
    val displayCompany = profile.companyCode.ifBlank { "1001" }

    val rawLoc = (record?.locationName ?: currentLocationName.ifBlank { profile.location }).trim()
    val isCoordinateString = rawLoc.startsWith("Lat", ignoreCase = true) ||
            rawLoc.matches(Regex("^-?\\d+(\\.\\d+)?,\\s*-?\\d+(\\.\\d+)?$"))
    val resolvedLocation = when {
        rawLoc.isNotBlank() && !isCoordinateString -> rawLoc
        profile.location.isNotBlank() && !profile.location.startsWith("Lat", ignoreCase = true) -> profile.location
        else -> "Karim Chamber Offices, Karachi"
    }
    // Clean any technical plus-code prefix e.g. "R2XH+RP6, Civil Lines" -> "Civil Lines"
    val cleanLoc = resolvedLocation
        .replace(Regex("^[A-Z0-9]{4,8}\\+[A-Z0-9]{2,4},\\s*"), "")
        .ifBlank { resolvedLocation }

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
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            if (isMarked && record != null) {
                val actionName = if (isTimeIn) "Time In" else "Time Out"

                // Uncluttered, compact confirmation header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.18f))
                            .border(1.5.dp, primaryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Confirmed",
                            tint = primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$actionName marked successfully!",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = record.formattedDateTime,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VtpOrange,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            } else {
                // Ready for attendance header
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
            HorizontalDivider(color = Color(0xFF1E283D), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Column-wise details layout (2 parallel columns side-by-side)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left Column: Name & Company Code
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ColumnInfoItem(
                        label = "EMPLOYEE NAME",
                        value = displayName
                    )
                    ColumnInfoItem(
                        label = "COMPANY CODE",
                        value = displayCompany
                    )
                }

                // Vertical Divider between columns
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF1E283D))
                )

                // Right Column: Employee Code & Location
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ColumnInfoItem(
                        label = "EMPLOYEE CODE",
                        value = displayCode
                    )
                    ColumnInfoItem(
                        label = "LOCATION",
                        value = cleanLoc
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF818EA6),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            lineHeight = 17.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

