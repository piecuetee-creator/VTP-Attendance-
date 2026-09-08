package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.EmployeeProfile
import com.example.ui.theme.DIBEmeraldPrimary
import com.example.ui.theme.vtpTextFieldColors
import com.example.util.DeviceInfoManager

@Composable
fun EmployeeProfileDialog(
    currentProfile: EmployeeProfile,
    onSaveProfile: (EmployeeProfile) -> Unit,
    onDismiss: () -> Unit,
    isFirstTimeSetup: Boolean = false
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(currentProfile.name) }
    var employeeId by remember { mutableStateOf(currentProfile.employeeId) }
    var designation by remember { mutableStateOf(currentProfile.designation) }
    var location by remember { mutableStateOf(currentProfile.location.ifBlank { "Karim Chamber Offices, Karachi" }) }
    var imei by remember {
        mutableStateOf(
            if (currentProfile.imei.isNotBlank()) currentProfile.imei
            else DeviceInfoManager.getDeviceImei(context)
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = {
        if (!isFirstTimeSetup) onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("employee_profile_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isFirstTimeSetup) "Set Up Employee Profile" else "Edit Employee Profile",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Enter your credentials and phone IMEI for socket tracking",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!isFirstTimeSetup) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Full Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Full Name *") },
                    placeholder = { Text("e.g. Your Name") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = DIBEmeraldPrimary)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_name"),
                    shape = RoundedCornerShape(12.dp),
                    colors = vtpTextFieldColors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Employee ID
                OutlinedTextField(
                    value = employeeId,
                    onValueChange = {
                        employeeId = it
                        errorMessage = null
                    },
                    label = { Text("Employee ID *") },
                    placeholder = { Text("e.g. 1024 or EMP-88") },
                    leadingIcon = {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = DIBEmeraldPrimary)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_id"),
                    shape = RoundedCornerShape(12.dp),
                    colors = vtpTextFieldColors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone IMEI (15 Digits)
                OutlinedTextField(
                    value = imei,
                    onValueChange = { input ->
                        val digitsOnly = input.filter { it.isDigit() }
                        if (digitsOnly.length <= 15) {
                            imei = digitsOnly
                        }
                        errorMessage = null
                    },
                    label = { Text("Phone IMEI (15 Digits) *") },
                    placeholder = { Text("e.g. 860003333257875") },
                    leadingIcon = {
                        Icon(Icons.Default.Smartphone, contentDescription = null, tint = DIBEmeraldPrimary)
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                imei = DeviceInfoManager.getDeviceImei(context)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Auto-detect Phone IMEI",
                                tint = DIBEmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    supportingText = {
                        Text(
                            text = "Sent to socket (avl.vtps.org:5200) with Lat/Long",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_imei"),
                    shape = RoundedCornerShape(12.dp),
                    colors = vtpTextFieldColors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Designation
                OutlinedTextField(
                    value = designation,
                    onValueChange = {
                        designation = it
                    },
                    label = { Text("Designation / Role") },
                    placeholder = { Text("e.g. Analyst, Officer, Engineer") },
                    leadingIcon = {
                        Icon(Icons.Default.Work, contentDescription = null, tint = DIBEmeraldPrimary)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_designation"),
                    shape = RoundedCornerShape(12.dp),
                    colors = vtpTextFieldColors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Work Location
                OutlinedTextField(
                    value = location,
                    onValueChange = {
                        location = it
                    },
                    label = { Text("Office / Work City") },
                    placeholder = { Text("e.g. Karim Chamber Offices, Karachi") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = DIBEmeraldPrimary)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_location"),
                    shape = RoundedCornerShape(12.dp),
                    colors = vtpTextFieldColors()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!isFirstTimeSetup) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Button(
                        onClick = {
                            val trimmedName = name.trim()
                            val trimmedId = employeeId.trim()
                            val cleanImei = DeviceInfoManager.sanitizeImei(imei.trim())
                            if (trimmedName.isBlank()) {
                                errorMessage = "Please enter your full name."
                                return@Button
                            }
                            if (trimmedId.isBlank()) {
                                errorMessage = "Please enter your employee ID."
                                return@Button
                            }
                            if (cleanImei.length < 15) {
                                errorMessage = "Please enter a valid 15-digit Phone IMEI."
                                return@Button
                            }

                            onSaveProfile(
                                EmployeeProfile(
                                    employeeId = trimmedId,
                                    name = trimmedName,
                                    designation = designation.trim().ifBlank { "Staff" },
                                    location = location.trim().ifBlank { "Karim Chamber Offices, Karachi" },
                                    imei = cleanImei
                                )
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("save_profile_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DIBEmeraldPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (isFirstTimeSetup) "Get Started" else "Save Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
