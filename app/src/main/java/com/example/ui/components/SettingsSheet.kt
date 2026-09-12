package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmployeeProfile
import com.example.model.SocketConfig
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.VtpOrange
import com.example.ui.theme.VtpOrangeContainer
import com.example.ui.theme.VtpOrangeDark
import com.example.ui.theme.vtpTextFieldColors
import com.example.util.DeviceInfoManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    profile: EmployeeProfile,
    config: SocketConfig,
    isUserLoggedIn: Boolean = false,
    onSaveProfile: (EmployeeProfile) -> Unit,
    onSaveConfig: (SocketConfig) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .fillMaxHeight(0.85f)
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
                        tint = VtpOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Settings & Protocol",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsContent(
                profile = profile,
                config = config,
                isUserLoggedIn = isUserLoggedIn,
                onSaveProfile = {
                    onSaveProfile(it)
                    onDismiss()
                },
                onSaveConfig = {
                    onSaveConfig(it)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Reusable Settings Content component that powers both the dedicated Settings Tab and bottom sheet.
 * Fulfills: "There must be setting of tab where necessary I.P., Server etc."
 */
@Composable
fun SettingsContent(
    profile: EmployeeProfile,
    config: SocketConfig,
    isUserLoggedIn: Boolean = false,
    onSaveProfile: (EmployeeProfile) -> Unit,
    onSaveConfig: (SocketConfig) -> Unit,
    modifier: Modifier = Modifier,
    onTestConnection: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var companyCode by remember { mutableStateOf(profile.companyCode.ifBlank { "1001" }) }
    var employeeCode by remember { mutableStateOf(profile.employeeCode.ifBlank { profile.employeeId.ifBlank { "0452" } }) }
    var serverDigits by remember { mutableStateOf(config.serverDigits.ifBlank { "01" }) }

    var imei by remember { mutableStateOf(config.imei) }
    var wsUrl by remember { mutableStateOf(config.wsUrl) }
    var tcpHost by remember { mutableStateOf(config.tcpHost) }
    var tcpPort by remember { mutableStateOf(config.tcpPort.toString()) }
    var useWs by remember { mutableStateOf(config.useWebSocket) }

    var saveConfirmation by remember { mutableStateOf(false) }

    val activeCompanyCode = if (isUserLoggedIn) profile.companyCode.ifBlank { companyCode } else companyCode
    val activeEmployeeCode = if (isUserLoggedIn) profile.employeeCode.ifBlank { employeeCode } else employeeCode

    // Live computed 15-digit IMEI
    val livePatternImei = remember(activeCompanyCode, activeEmployeeCode, serverDigits) {
        DeviceInfoManager.buildVtpImei(activeCompanyCode, activeEmployeeCode, serverDigits, context)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Server & Network Configuration (I.P., Server, Port, Protocol)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        tint = VtpOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SERVER & I.P. CONFIGURATION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = VtpOrange,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WebSocket Mode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (useWs) "Online streaming via ws://" else "Raw TCP socket",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = useWs,
                        onCheckedChange = { useWs = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = VtpOrange
                        )
                    )
                }

                // Server Host / IP Address
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = tcpHost,
                        onValueChange = {
                            tcpHost = it
                            if (wsUrl.contains("://")) {
                                val protocol = wsUrl.substringBefore("://")
                                val currentPort = tcpPort.toIntOrNull() ?: 5200
                                wsUrl = "$protocol://$it:$currentPort"
                            }
                        },
                        label = { Text("Server I.P. / Host") },
                        placeholder = { Text("e.g. avl.vtps.org") },
                        modifier = Modifier
                            .weight(2f)
                            .testTag("tcp_host_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = vtpTextFieldColors()
                    )

                    OutlinedTextField(
                        value = tcpPort,
                        onValueChange = {
                            tcpPort = it.filter { ch -> ch.isDigit() }
                            if (wsUrl.contains("://")) {
                                val protocol = wsUrl.substringBefore("://")
                                wsUrl = "$protocol://$tcpHost:$it"
                            }
                        },
                        label = { Text("Port") },
                        placeholder = { Text("5200") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tcp_port_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = vtpTextFieldColors()
                    )
                }

                // WebSocket Full URL
                OutlinedTextField(
                    value = wsUrl,
                    onValueChange = { wsUrl = it },
                    label = { Text("WebSocket URL (ws://)") },
                    placeholder = { Text("ws://avl.vtps.org:5200") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ws_url_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = vtpTextFieldColors()
                )

                // Server Code (2 digits) - Fixed by default, adjustable in settings
                OutlinedTextField(
                    value = serverDigits,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() }.take(2)
                        serverDigits = clean
                        val recomputed = DeviceInfoManager.buildVtpImei(activeCompanyCode, activeEmployeeCode, clean, context)
                        imei = recomputed
                    },
                    label = { Text("Server Code (2 digits)") },
                    placeholder = { Text("01") },
                    leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null, tint = VtpOrange) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_server_code_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = vtpTextFieldColors(),
                    supportingText = {
                        Text(
                            text = "Protocol server code (default: 01). Automatically updates terminal IMEI.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        // Section 2: 15-Digit Terminal IMEI & Pattern (User specification)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = VtpOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TERMINAL IMEI & PROTOCOL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VtpOrange,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(VtpOrangeContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "15 Digits",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = VtpOrangeDark
                        )
                    }
                }

                // 15-Digit IMEI Visualizer (Locked once user is logged in)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = imei.ifBlank { livePatternImei },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = VtpOrange,
                                letterSpacing = 1.sp
                            )
                            if (isUserLoggedIn) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF1E293B))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LOCKED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFBBF24)
                                    )
                                }
                            }
                        }
                    }
                }

                if (isUserLoggedIn) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Terminal IMEI is securely generated with active user credentials.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Active Server Code: $serverDigits (adjustable in Server Configuration above)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = VtpOrange
                        )
                    }
                } else {
                    // Quick Company & Employee Code inputs (Only editable when not logged in)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = companyCode,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }.take(4)
                                companyCode = clean
                                imei = DeviceInfoManager.buildVtpImei(companyCode, employeeCode, serverDigits, context)
                            },
                            label = { Text("Company (4)") },
                            placeholder = { Text("1001") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = VtpOrange) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = vtpTextFieldColors()
                        )

                        OutlinedTextField(
                            value = employeeCode,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }.take(4)
                                employeeCode = clean
                                imei = DeviceInfoManager.buildVtpImei(companyCode, employeeCode, serverDigits, context)
                            },
                            label = { Text("Employee (4)") },
                            placeholder = { Text("0452") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = VtpOrange) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = vtpTextFieldColors()
                        )
                    }

                    OutlinedTextField(
                        value = imei,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(15)
                            imei = digits
                        },
                        label = { Text("Override IMEI (15 Digits)") },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    imei = livePatternImei
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset Terminal IMEI",
                                    tint = VtpOrange
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("imei_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = vtpTextFieldColors()
                    )
                }
            }
        }

        // Action Buttons: Save & Test
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .shadow(6.dp, RoundedCornerShape(12.dp), ambientColor = VtpOrange, spotColor = VtpOrange)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(Color(0xFFFF4500), Color(0xFFFF6600), Color(0xFFFFA040))
                    )
                )
        ) {
            Button(
                onClick = {
                    val port = tcpPort.toIntOrNull() ?: 5200
                    val cleanServer = serverDigits.filter { it.isDigit() }.let {
                        if (it.length > 2) it.takeLast(2) else it.padStart(2, '0')
                    }.ifBlank { "01" }

                    val cleanImei = if (imei.isNotBlank() && imei != livePatternImei && !isUserLoggedIn) {
                        DeviceInfoManager.sanitizeImei(imei)
                    } else {
                        DeviceInfoManager.buildVtpImei(activeCompanyCode, activeEmployeeCode, cleanServer, context)
                    }

                    onSaveConfig(
                        config.copy(
                            imei = cleanImei,
                            wsUrl = wsUrl.trim(),
                            tcpHost = tcpHost.trim(),
                            tcpPort = port,
                            useWebSocket = useWs,
                            serverDigits = cleanServer
                        )
                    )
                    // Profile credentials (name, desig, loc, codes) remain locked as entered at login,
                    // but IMEI and serverDigits are updated with the new server code
                    onSaveProfile(
                        profile.copy(
                            imei = cleanImei,
                            serverDigits = cleanServer
                        )
                    )
                    imei = cleanImei
                    saveConfirmation = true
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (saveConfirmation) "Saved Successfully!" else "Save Configuration",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (onTestConnection != null) {
            Button(
                onClick = onTestConnection,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("test_connection_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(imageVector = Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Test Server Connection",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
