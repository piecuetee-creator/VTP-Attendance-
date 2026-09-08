package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmployeeProfile
import com.example.model.SocketConfig
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DIBEmeraldPrimary
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    profile: EmployeeProfile,
    config: SocketConfig,
    onSaveProfile: (EmployeeProfile) -> Unit,
    onSaveConfig: (SocketConfig) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    var imei by remember { mutableStateOf(config.imei) }
    var wsUrl by remember { mutableStateOf(config.wsUrl) }
    var tcpHost by remember { mutableStateOf(config.tcpHost) }
    var tcpPort by remember { mutableStateOf(config.tcpPort.toString()) }
    var useWs by remember { mutableStateOf(config.useWebSocket) }

    var empId by remember { mutableStateOf(profile.employeeId) }
    var empName by remember { mutableStateOf(profile.name) }
    var empDesig by remember { mutableStateOf(profile.designation) }
    var empLoc by remember { mutableStateOf(profile.location) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceCard,
        modifier = Modifier.testTag("settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .fillMaxHeight(0.85f)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = DIBEmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Settings & Protocol",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // GT06 Protocol Settings Section
            Text(
                text = "GT06 SOCKET GATEWAY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DIBEmeraldPrimary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = imei,
                onValueChange = { imei = it },
                label = { Text("Device IMEI (15 Digits)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("imei_input"),
                shape = RoundedCornerShape(10.dp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Use WebSocket Mode",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (useWs) "Streaming via ws:// protocol" else "Raw TCP socket",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = useWs,
                    onCheckedChange = { useWs = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = DIBEmeraldPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = wsUrl,
                onValueChange = { wsUrl = it },
                label = { Text("WebSocket Gateway URL") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ws_url_input"),
                shape = RoundedCornerShape(10.dp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = tcpHost,
                    onValueChange = { tcpHost = it },
                    label = { Text("TCP Host") },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(10.dp),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = tcpPort,
                    onValueChange = { tcpPort = it },
                    label = { Text("Port") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = textFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(16.dp))

            // Employee Profile Section
            Text(
                text = "EMPLOYEE PROFILE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DIBEmeraldPrimary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = empId,
                onValueChange = { empId = it },
                label = { Text("Employee ID") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = empName,
                onValueChange = { empName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = empDesig,
                onValueChange = { empDesig = it },
                label = { Text("Designation") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = empLoc,
                onValueChange = { empLoc = it },
                label = { Text("Office Location") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Action Button
            Button(
                onClick = {
                    val port = tcpPort.toIntOrNull() ?: 5200
                    onSaveConfig(
                        config.copy(
                            imei = imei,
                            wsUrl = wsUrl,
                            tcpHost = tcpHost,
                            tcpPort = port,
                            useWebSocket = useWs
                        )
                    )
                    onSaveProfile(
                        profile.copy(
                            employeeId = empId,
                            name = empName,
                            designation = empDesig,
                            location = empLoc
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DIBEmeraldPrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Configuration",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = DIBEmeraldPrimary,
    unfocusedBorderColor = BorderSubtle,
    focusedLabelColor = DIBEmeraldPrimary,
    cursorColor = DIBEmeraldPrimary
)
