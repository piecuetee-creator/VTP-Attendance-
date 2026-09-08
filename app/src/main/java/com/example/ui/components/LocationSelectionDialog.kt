package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.location.Coordinates
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DIBEmeraldContainer
import com.example.ui.theme.DIBEmeraldDark
import com.example.ui.theme.DIBEmeraldPrimary
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TimeInGreen
import com.example.ui.theme.vtpTextFieldColors

@Composable
fun LocationSelectionDialog(
    currentCoordinates: Coordinates,
    pakistanPresets: List<Coordinates>,
    onSelectCoordinates: (Coordinates) -> Unit,
    onUseHardwareGps: () -> Unit,
    onDismiss: () -> Unit
) {
    var showCustomInput by remember { mutableStateOf(false) }
    var customLat by remember { mutableStateOf(if (currentCoordinates.latitude != 0.0) currentCoordinates.latitude.toString() else "24.8607") }
    var customLon by remember { mutableStateOf(if (currentCoordinates.longitude != 0.0) currentCoordinates.longitude.toString() else "67.0011") }
    var customName by remember { mutableStateOf(currentCoordinates.addressName ?: "Karachi, Pakistan") }
    var customError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("location_selection_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Dialog Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Select Attendance Location",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose your Pakistan workplace or phone GPS",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Option 1: Live Hardware GPS
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onUseHardwareGps()
                            onDismiss()
                        },
                    color = SurfaceCanvas,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(TimeInGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = TimeInGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Live Device GPS (Satellite)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Uses physical phone sensor (Pakistan GPS when running APK on phone)",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PAKISTAN PRESETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DIBEmeraldPrimary,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Pakistan Presets list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCanvas)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                ) {
                    pakistanPresets.forEachIndexed { index, preset ->
                        val isSelected = currentCoordinates.addressName == preset.addressName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCoordinates(preset)
                                    onDismiss()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationCity,
                                    contentDescription = null,
                                    tint = if (isSelected) DIBEmeraldPrimary else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = preset.addressName ?: "Pakistan",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = preset.formatCoordinates(),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = DIBEmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (index < pakistanPresets.lastIndex) {
                            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Option: Enter Custom Coordinates / Address
                if (!showCustomInput) {
                    Button(
                        onClick = { showCustomInput = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceCanvas,
                            contentColor = DIBEmeraldPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enter Custom Address / Coordinates", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCanvas)
                            .padding(12.dp)
                    ) {
                        Text("Custom Coordinates / City", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Location Name") },
                            placeholder = { Text("e.g. My Office, Karachi, Pakistan") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = vtpTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = customLat,
                                onValueChange = { customLat = it },
                                label = { Text("Latitude") },
                                placeholder = { Text("24.8607") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = vtpTextFieldColors()
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedTextField(
                                value = customLon,
                                onValueChange = { customLon = it },
                                label = { Text("Longitude") },
                                placeholder = { Text("67.0011") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = vtpTextFieldColors()
                            )
                        }

                        if (customError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(customError ?: "", color = Color(0xFFEF4444), fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val lat = customLat.trim().toDoubleOrNull()
                                val lon = customLon.trim().toDoubleOrNull()
                                if (lat == null || lon == null) {
                                    customError = "Please enter valid decimal coordinates."
                                    return@Button
                                }
                                onSelectCoordinates(
                                    Coordinates(
                                        latitude = lat,
                                        longitude = lon,
                                        isRealGps = true,
                                        accuracyMeters = 5.0f,
                                        addressName = customName.trim().ifBlank { "Custom Location, Pakistan" },
                                        provider = "Custom Override"
                                    )
                                )
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DIBEmeraldPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Apply Custom Location", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
