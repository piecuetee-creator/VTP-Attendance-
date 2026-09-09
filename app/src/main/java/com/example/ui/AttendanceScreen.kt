package com.example.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.shadow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.network.ConnectionStatus
import com.example.ui.components.AttendanceActionCards
import com.example.ui.components.AttendanceHeader
import com.example.ui.components.AttendanceLocationCard
import com.example.ui.components.AttendanceSecondaryActions
import com.example.ui.components.AttendanceSuccessDialog
import com.example.ui.components.ConsoleContent
import com.example.ui.components.ConsoleLogSheet
import com.example.ui.components.CustomerAuthScreen
import com.example.ui.components.EmployeeProfileDialog
import com.example.ui.components.LocationSelectionDialog
import com.example.ui.components.SettingsContent
import com.example.ui.components.SettingsSheet
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VtpBlack
import com.example.ui.theme.VtpOrange
import com.example.ui.theme.VtpOrangeContainer
import com.example.ui.theme.VtpOrangeDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val employeeProfile by viewModel.employeeProfile.collectAsStateWithLifecycle()
    val socketConfig by viewModel.socketConfig.collectAsStateWithLifecycle()
    val lastTimeIn by viewModel.lastTimeIn.collectAsStateWithLifecycle()
    val lastTimeOut by viewModel.lastTimeOut.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val currentCoords by viewModel.currentCoordinates.collectAsStateWithLifecycle()
    val isLoadingLoc by viewModel.isLoadingLocation.collectAsStateWithLifecycle()
    val isProcTimeIn by viewModel.isProcessingTimeIn.collectAsStateWithLifecycle()
    val isProcTimeOut by viewModel.isProcessingTimeOut.collectAsStateWithLifecycle()
    val activeDialogRecord by viewModel.activeDialogRecord.collectAsStateWithLifecycle()
    val isDialogAlreadyMarked by viewModel.isDialogAlreadyMarked.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    // Tab Navigation: 0 = Attendance, 1 = Settings, 2 = Console
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    var showConsoleSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    val consoleSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Live Clock timer
    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val clockFormat = SimpleDateFormat("EEEE, dd MMMM • hh:mm:ss a", Locale.getDefault())
        while (true) {
            currentTimeString = clockFormat.format(Date())
            delay(1000)
        }
    }

    // Permission launcher for Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.refreshLocation()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Login Page: User enters only company and employee code
    if (!authState.isAuthenticated) {
        CustomerAuthScreen(
            employeeProfile = employeeProfile,
            onLoginSuccess = { comp, emp ->
                viewModel.loginWithCodes(comp, emp)
            },
            onClose = {
                viewModel.dismissAuth()
            },
            modifier = modifier
        )
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("attendance_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (selectedTab != 0) {
                AttendanceHeader(
                    connectionStatus = connectionStatus,
                    onOpenTerminal = { selectedTab = 2 },
                    onOpenSettings = { selectedTab = 1 },
                    onLock = { viewModel.logout() }
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Attendance") },
                    label = {
                        Text(
                            text = "Attendance",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = VtpOrange,
                        indicatorColor = VtpOrange
                    ),
                    modifier = Modifier.testTag("tab_attendance")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = {
                        Text(
                            text = "Settings",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = VtpOrange,
                        indicatorColor = VtpOrange
                    ),
                    modifier = Modifier.testTag("tab_settings")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = "Console") },
                    label = {
                        Text(
                            text = "Console",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = VtpOrange,
                        indicatorColor = VtpOrange
                    ),
                    modifier = Modifier.testTag("tab_console")
                )
            }
        },
        containerColor = SurfaceCanvas
    ) { paddingValues ->
        when (selectedTab) {
            // TAB 0: ATTENDANCE LANDING PAGE - Matching snippet layout:
            // Centered Presence Logo (replacing DIB) -> "Presence" -> "Powered by VTP" -> (Time In) & (Time Out) cards
            0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Row: Connection Status Pill & Quick Action Icons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Connection Status Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF5F3F0),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D8))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (connectionStatus == ConnectionStatus.CONNECTED) Color(0xFF10B981)
                                            else Color(0xFFF59E0B)
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (connectionStatus == ConnectionStatus.CONNECTED) "GT06 Online" else "GT06 Standby",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF524840)
                                )
                            }
                        }

                        // Quick Navigation Actions (Console, Settings, Lock)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { selectedTab = 2 },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = "Console",
                                    tint = Color(0xFF787068),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { selectedTab = 1 },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color(0xFF787068),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.logout() },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock Session",
                                    tint = Color(0xFF787068),
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    // Main Center Section: Logo -> Presence -> Powered by VTP -> 2 Action Layouts
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Presence App Logo (replacing DIB logo as shown in user snippet)
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = VtpOrange.copy(alpha = 0.25f), spotColor = VtpOrange)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFFF3D00), Color(0xFFFF6600), Color(0xFFFFA040))
                                    )
                                )
                                .border(
                                    1.5.dp,
                                    Brush.linearGradient(
                                        listOf(Color.White.copy(alpha = 0.85f), Color(0xFFFF7A00))
                                    ),
                                    RoundedCornerShape(24.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_vtp_presence_logo),
                                contentDescription = "Presence Logo",
                                modifier = Modifier
                                    .size(82.dp)
                                    .clip(RoundedCornerShape(24.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // App name underneath: "Presence"
                        Text(
                            text = "Presence",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Underneath: "Powered by VTP"
                        Text(
                            text = "Powered by VTP",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = VtpOrangeDark,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(34.dp))

                        // 2 layouts: (Time In) and (Time Out) side-by-side
                        AttendanceActionCards(
                            lastTimeIn = lastTimeIn,
                            lastTimeOut = lastTimeOut,
                            isProcessingTimeIn = isProcTimeIn,
                            isProcessingTimeOut = isProcTimeOut,
                            onTimeInClick = { viewModel.onTimeInClicked() },
                            onTimeOutClick = { viewModel.onTimeOutClicked() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // TAB 1: SETTINGS (I.P., Server, Port, WebSocket, 15-Digit Terminal IMEI)
            1 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    SettingsContent(
                        profile = employeeProfile,
                        config = socketConfig,
                        onSaveProfile = { newProfile ->
                            viewModel.updateProfile(newProfile)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Profile saved successfully")
                            }
                        },
                        onSaveConfig = { newConfig ->
                            viewModel.updateSocketConfig(newConfig)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Server & Terminal config saved successfully")
                            }
                        },
                        onTestConnection = {
                            viewModel.sendTestLoginPacket()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Testing connection to ${socketConfig.tcpHost}:${socketConfig.tcpPort}...")
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // TAB 2: LIVE CONSOLE LOGS
            2 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    ConsoleContent(
                        logs = logs,
                        connectionStatus = connectionStatus,
                        targetUrl = if (socketConfig.useWebSocket) socketConfig.wsUrl else "${socketConfig.tcpHost}:${socketConfig.tcpPort}",
                        onClearLogs = { viewModel.clearLogs() },
                        onSendTestLogin = { viewModel.sendTestLoginPacket() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Employee Profile Setup/Edit Dialog
    if (showProfileDialog) {
        EmployeeProfileDialog(
            currentProfile = employeeProfile,
            onSaveProfile = { newProfile ->
                viewModel.updateProfile(newProfile)
                showProfileDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Profile updated: ${newProfile.name}")
                }
            },
            onDismiss = { showProfileDialog = false },
            isFirstTimeSetup = employeeProfile.name.isBlank()
        )
    }

    // Location Selection Dialog
    if (showLocationPicker) {
        LocationSelectionDialog(
            currentCoordinates = currentCoords,
            pakistanPresets = viewModel.pakistanPresets,
            onSelectCoordinates = { coords ->
                viewModel.selectLocation(coords)
                showLocationPicker = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Location set to: ${coords.addressName ?: coords.formatCoordinates()}")
                }
            },
            onUseHardwareGps = {
                viewModel.useHardwareGps()
                showLocationPicker = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Switched to Live Hardware GPS")
                }
            },
            onDismiss = { showLocationPicker = false }
        )
    }

    // Success Confirmation Dialog
    activeDialogRecord?.let { record ->
        AttendanceSuccessDialog(
            record = record,
            profile = employeeProfile,
            isAlreadyMarked = isDialogAlreadyMarked,
            onDismiss = { viewModel.dismissDialog() }
        )
    }

    // Modal Bottom Sheets for direct deep-links if triggered
    if (showConsoleSheet) {
        ConsoleLogSheet(
            logs = logs,
            connectionStatus = connectionStatus,
            targetUrl = if (socketConfig.useWebSocket) socketConfig.wsUrl else "${socketConfig.tcpHost}:${socketConfig.tcpPort}",
            onClearLogs = { viewModel.clearLogs() },
            onSendTestLogin = { viewModel.sendTestLoginPacket() },
            onDismiss = { showConsoleSheet = false },
            sheetState = consoleSheetState
        )
    }

    if (showSettingsSheet) {
        SettingsSheet(
            profile = employeeProfile,
            config = socketConfig,
            onSaveProfile = { newProfile ->
                viewModel.updateProfile(newProfile)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Profile updated successfully")
                }
            },
            onSaveConfig = { newConfig ->
                viewModel.updateSocketConfig(newConfig)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Socket config updated successfully")
                }
            },
            onDismiss = { showSettingsSheet = false },
            sheetState = settingsSheetState
        )
    }
}
