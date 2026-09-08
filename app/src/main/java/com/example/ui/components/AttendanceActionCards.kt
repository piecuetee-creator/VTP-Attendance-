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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AttendanceRecord
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TimeInGreen
import com.example.ui.theme.TimeInGreenBorder
import com.example.ui.theme.TimeInGreenContainer
import com.example.ui.theme.TimeOutAmber
import com.example.ui.theme.TimeOutAmberBorder
import com.example.ui.theme.TimeOutAmberContainer

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
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Section Header (matching Screenshot 1 pattern)
        Text(
            text = "Attendance",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = 0.2.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )

        Text(
            text = "Please select an option to mark your attendance",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 18.dp)
        )

        // Action Cards: Time In & Time Out side-by-side in a Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // TIME IN CARD (Left)
            AttendanceGridCard(
                title = "Time In",
                statusLabel = if (lastTimeIn != null) "Marked" else "ACC ON",
                isTimeIn = true,
                markedRecord = lastTimeIn,
                isProcessing = isProcessingTimeIn,
                onClick = onTimeInClick,
                testTag = "time_in_button",
                modifier = Modifier.weight(1f)
            )

            // TIME OUT CARD (Right)
            AttendanceGridCard(
                title = "Time Out",
                statusLabel = if (lastTimeOut != null) "Marked" else "ACC OFF",
                isTimeIn = false,
                markedRecord = lastTimeOut,
                isProcessing = isProcessingTimeOut,
                onClick = onTimeOutClick,
                testTag = "time_out_button",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AttendanceGridCard(
    title: String,
    statusLabel: String,
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
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    val primaryColor = if (isTimeIn) TimeInGreen else TimeOutAmber
    val containerBg = if (isTimeIn) TimeInGreenContainer else TimeOutAmberContainer
    val borderColor = if (isTimeIn) TimeInGreenBorder else TimeOutAmberBorder
    val iconVector = if (isTimeIn) Icons.Default.PlayArrow else Icons.Default.Stop

    Surface(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = primaryColor.copy(alpha = 0.15f),
                spotColor = primaryColor.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, borderColor.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isProcessing,
                onClick = onClick
            )
            .testTag(testTag),
        color = SurfaceCard,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circle Icon Container
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(containerBg)
                    .border(2.dp, primaryColor.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp),
                        color = primaryColor,
                        strokeWidth = 3.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(primaryColor),
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

            // Card Title (Time In / Time Out)
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Status Badge / Time
            if (markedRecord != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(containerBg)
                        .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = markedRecord.formattedDateTime.substringAfter(" "),
                            color = primaryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Text(
                    text = "Tap to Mark",
                    fontSize = 12.sp,
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // GT06 Protocol ACC Status Subtitle
            Text(
                text = if (isTimeIn) "Ignition ON (GT06)" else "Ignition OFF (GT06)",
                fontSize = 10.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
