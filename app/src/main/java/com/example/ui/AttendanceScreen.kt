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
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.network.ConnectionStatus
import com.example.ui.components.AttendanceActionCards
import com.example.ui.components.AttendanceConfirmationCard
import com.example.ui.components.AttendanceHeader
import com.example.ui.components.AttendanceLocationCard
import androidx.biometric.BiometricPrompt
import com.example.ui.components.AttendanceSecondaryActions
import com.example.ui.components.ConsoleContent
import com.example.ui.components.ConsoleLogSheet
import com.example.ui.components.CustomerAuthScreen
import com.example.ui.components.EmployeeProfileDialog
import com.example.ui.components.LocationSelectionDialog
import com.example.ui.components.SettingsContent
import com.example.ui.components.SettingsSheet
import com.example.util.BiometricAuthManager
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
    val isServerSyncing by viewModel.isServerSyncing.collectAsStateWithLifecycle()
    val activeDialogRecord by viewModel.activeDialogRecord.collectAsStateWithLifecycle()
    val isDialogAlreadyMarked by viewModel.isDialogAlreadyMarked.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    val timeInRec = lastTimeIn
    val timeOutRec = lastTimeOut
    val latestAttendanceRecord = remember(timeInRec, timeOutRec) {
        when {
            timeInRec != null && timeOutRec != null -> {
                if (timeOutRec.timestamp >= timeInRec.timestamp) timeOutRec else timeInRec
            }
            timeOutRec != null -> timeOutRec
            else -> timeInRec
        }
    }

    // Authentication check: Is the user logged in with Company & Employee codes?
    val isUserLoggedIn = authState.isAuthenticated &&
            employeeProfile.companyCode.isNotBlank() &&
            (employeeProfile.employeeCode.isNotBlank() || employeeProfile.employeeId.isNotBlank())

    var showConsoleSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var showLoginSheet by remember { mutableStateOf(false) }
    val consoleSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Real Hardware Biometric Trigger for Time In & Time Out
    fun triggerBiometricAttendance(isTimeIn: Boolean) {
        val activity = BiometricAuthManager.findFragmentActivity(context)
        val actionName = if (isTimeIn) "Time In" else "Time Out"
        val empDisplayName = if (employeeProfile.name.isNotBlank()) employeeProfile.name else "Employee #${employeeProfile.employeeCode.ifBlank { employeeProfile.employeeId }}"

        if (activity != null) {
            val availability = BiometricAuthManager.checkBiometricAvailability(context)
            when (availability) {
                is BiometricAuthManager.BiometricAvailability.Available -> {
                    BiometricAuthManager.promptBiometric(
                        activity = activity,
                        title = "Presence $actionName",
                        subtitle = "Verify biometric identity for $empDisplayName",
                        onSuccess = {
                            if (isTimeIn) {
                                viewModel.onTimeInClicked()
                            } else {
                                viewModel.onTimeOutClicked()
                            }
                        },
                        onError = { errorCode, errString ->
                            if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                                errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                                errorCode != BiometricPrompt.ERROR_CANCELED
                            ) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Biometric error: $errString")
                                }
                            }
                        },
                        onFailed = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Biometric not recognized. Attendance not recorded.")
                            }
                        }
                    )
                }
                is BiometricAuthManager.BiometricAvailability.Unavailable -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Biometric verification required: ${availability.reason}")
                    }
                }
            }
        } else {
            if (isTimeIn) {
                viewModel.onTimeInClicked()
            } else {
                viewModel.onTimeOutClicked()
            }
        }
    }

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
        try {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } catch (e: Exception) {
            // Gracefully handle permission launch if activity is finishing or restricted
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("attendance_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {},
        containerColor = Color(0xFF090D16) // Dark Theme Restored
    ) { paddingValues ->
        // Unified Single Page Layout:
        if (!isUserLoggedIn || showLoginSheet) {
            CustomerAuthScreen(
                employeeProfile = employeeProfile,
                isAuthenticated = isUserLoggedIn,
                onLoginSuccess = { comp, emp, name, desig, loc ->
                    viewModel.loginWithDetails(comp, emp, name, desig, loc)
                    showLoginSheet = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Logged in successfully as $name")
                    }
                },
                onLogout = {
                    viewModel.logout()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Session logged out")
                    }
                },
                onNavigateToAttendance = {
                    if (isUserLoggedIn) {
                        showLoginSheet = false
                    }
                },
                onClose = if (isUserLoggedIn) { { showLoginSheet = false } } else null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            // First Page: Clean, header-less landing screen (Dark Theme)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Subtle top-right options dropdown for Settings & Logout without any header banner
                var showOptionsMenu by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, end = 12.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = { showOptionsMenu = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF131B2A).copy(alpha = 0.6f))
                            .testTag("landing_options_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings & Connection") },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = VtpOrange)
                            },
                            onClick = {
                                showOptionsMenu = false
                                showSettingsSheet = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Console Logs") },
                            leadingIcon = {
                                Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF94A3B8))
                            },
                            onClick = {
                                showOptionsMenu = false
                                showConsoleSheet = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Log Out") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFEF4444))
                            },
                            onClick = {
                                showOptionsMenu = false
                                viewModel.logout()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Logged out successfully.")
                                }
                            }
                        )
                    }
                }

                // Centered Container: Neatly aligned Time In & Time Out container with Details Card directly behind/below it
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Presence P Logo
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .shadow(12.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.35f))
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.presence_p_logo),
                            contentDescription = "Presence Logo",
                            modifier = Modifier
                                .size(78.dp)
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Underneath: "Presence"
                    Text(
                        text = "Presence",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Underneath: "Powered by VTP"
                    Text(
                        text = "Powered by VTP",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VtpOrange,
                        letterSpacing = 0.4.sp
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Time In and Time Out side-by-side container
                    AttendanceActionCards(
                        lastTimeIn = lastTimeIn,
                        lastTimeOut = lastTimeOut,
                        isProcessingTimeIn = isProcTimeIn,
                        isProcessingTimeOut = isProcTimeOut,
                        onTimeInClick = {
                            triggerBiometricAttendance(isTimeIn = true)
                        },
                        onTimeOutClick = {
                            triggerBiometricAttendance(isTimeIn = false)
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Confirmation Card underneath Time In & Time Out
                    val rawLoc = (latestAttendanceRecord?.locationName ?: currentCoords.addressName ?: employeeProfile.location).trim()
                    val isCoordinateString = rawLoc.startsWith("Lat", ignoreCase = true) ||
                            rawLoc.matches(Regex("^-?\\d+(\\.\\d+)?,\\s*-?\\d+(\\.\\d+)?$"))
                    val resolvedLocation = when {
                        rawLoc.isNotBlank() && !isCoordinateString -> rawLoc
                        employeeProfile.location.isNotBlank() && !employeeProfile.location.startsWith("Lat", ignoreCase = true) -> employeeProfile.location
                        else -> "Karim Chamber Offices, Karachi"
                    }

                    AttendanceConfirmationCard(
                        record = latestAttendanceRecord,
                        profile = employeeProfile,
                        currentLocationName = resolvedLocation,
                        isSyncing = isServerSyncing,
                        modifier = Modifier.fillMaxWidth()
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

    // Modal Bottom Sheets
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
            isUserLoggedIn = isUserLoggedIn,
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
