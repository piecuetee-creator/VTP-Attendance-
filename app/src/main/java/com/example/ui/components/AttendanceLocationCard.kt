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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.location.Coordinates
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TimeInGreen
import com.example.ui.theme.VtpOrange
import com.example.ui.theme.VtpOrangeContainer
import com.example.ui.theme.VtpOrangeDark

@Composable
fun AttendanceLocationCard(
    coordinates: Coordinates,
    locationName: String,
    isLoadingLocation: Boolean,
    onRefreshLocation: () -> Unit,
    onOpenLocationPicker: () -> Unit,
    onQuickSwitchToPakistan: () -> Unit,
    onRequestPermission: (() -> Unit)? = null,
    imei: String = "",
    modifier: Modifier = Modifier
) {
    val displayLocationName = coordinates.addressName?.takeIf { it.isNotBlank() } ?: locationName

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (coordinates.isRealGps && !coordinates.isCloudEmulator) TimeInGreen.copy(alpha = 0.15f)
                                else VtpOrangeContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (coordinates.isRealGps && !coordinates.isCloudEmulator) Icons.Default.GpsFixed else Icons.Default.LocationOn,
                            contentDescription = "Location Pin",
                            tint = if (coordinates.isRealGps && !coordinates.isCloudEmulator) TimeInGreen else VtpOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenLocationPicker() }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (coordinates.isCloudEmulator) "Cloud Virtual GPS (US)" else "Attendance Location",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (coordinates.isCloudEmulator) VtpOrange else if (coordinates.isRealGps) TimeInGreen else TextSecondary
                            )
                            if (coordinates.isRealGps && !coordinates.isCloudEmulator) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Actual GPS Active",
                                    tint = TimeInGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Text(
                            text = displayLocationName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 2
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenLocationPicker,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceCanvas)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .testTag("change_location_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditLocation,
                            contentDescription = "Change Location",
                            tint = VtpOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onRefreshLocation,
                        enabled = !isLoadingLocation,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceCanvas)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .testTag("refresh_location_button")
                    ) {
                        if (isLoadingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = VtpOrange,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh GPS",
                                tint = VtpOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Coordinates Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = SurfaceCanvas
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = if (coordinates.isCloudEmulator) VtpOrange else if (coordinates.isRealGps) TimeInGreen else VtpOrange,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = coordinates.formatCoordinates(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (coordinates.isCloudEmulator) VtpOrangeContainer
                                else if (coordinates.isRealGps) TimeInGreen.copy(alpha = 0.12f)
                                else VtpOrangeContainer
                            )
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (coordinates.isCloudEmulator) {
                                "Cloud Emulator GPS"
                            } else if (coordinates.isRealGps) {
                                val acc = if (coordinates.accuracyMeters > 0) " (±${coordinates.accuracyMeters.toInt()}m)" else ""
                                "Actual Location$acc"
                            } else {
                                "Default Location"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (coordinates.isCloudEmulator) VtpOrangeDark else if (coordinates.isRealGps) TimeInGreen else VtpOrangeDark
                        )
                    }
                }
            }

            // Phone IMEI Telemetry Row
            if (imei.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceCanvas
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = null,
                                tint = VtpOrange,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "IMEI: $imei",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(VtpOrangeContainer)
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "15-Digit IMEI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = VtpOrangeDark
                            )
                        }
                    }
                }
            }

            // Cloud Emulator Notice & 1-tap Pakistan Switch
            if (coordinates.isCloudEmulator) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = VtpOrangeContainer.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VtpOrange.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = VtpOrangeDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Running in Cloud Web Emulator (US Datacenter)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = VtpOrangeDark
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "On your real Android phone in Pakistan, the APK uses your physical GPS. For this preview, tap below to switch to your Pakistan location:",
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            listOf(Color(0xFFEA580C), Color(0xFFFF6600))
                                        )
                                    )
                            ) {
                                Button(
                                    onClick = onQuickSwitchToPakistan,
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Set to Karachi, Pakistan", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = onOpenLocationPicker,
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(34.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Choose...", fontSize = 11.sp, color = VtpOrangeDark)
                            }
                        }
                    }
                }
            }
        }
    }
}
