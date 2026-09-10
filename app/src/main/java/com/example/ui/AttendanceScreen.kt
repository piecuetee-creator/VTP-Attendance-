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
import com.example.ui.components.AttendanceHeader
import com.example.ui.components.AttendanceLocationCard
import com.example.ui.components.AttendanceSecondaryActions
import com.example.ui.components.AttendanceSuccessDialog
import com.example.ui.components.BiometricVerificationSheet
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
    val activeDialogRecord by viewModel.activeDialogRecord.collectAsStateWithLifecycle()
    val isDialogAlreadyMarked by viewModel.isDialogAlreadyMarked.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    // Authentication check: Is the user logged in with Company & Employee codes?
    val isUserLoggedIn = authState.isAuthenticated &&
            employeeProfile.companyCode.isNotBlank() &&
            (employeeProfile.employeeCode.isNotBlank() || employeeProfile.employeeId.isNotBlank())

    var showConsoleSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var showLoginSheet by remember { mutableStateOf(false) }
    // Biometric Verification Bottom Sheet state: null = hidden, true = Time In, false = Time Out
    var pendingBiometricIsTimeIn by remember { mutableStateOf<Boolean?>(null) }
    val consoleSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
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
                onLoginSuccess = { comp, emp ->
                    viewModel.loginWithCodes(comp, emp)
                    showLoginSheet = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Logged in successfully as $emp")
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
                    
                    // --- LOGO RESTORED HERE ---
                    Image(
                        painter = painterResource(id = R.drawable.vtp_logo),
                        contentDescription = "VTP Logo",
                        modifier = Modifier
                            .size(110.dp) // Change this to make the logo bigger or smaller
                            .padding(bottom = 24.dp) // Adds space between the logo and the cards below
                            .clip(RoundedCornerShape(16.dp)), // Rounds the corners of the logo
                        contentScale = ContentScale.Crop
                    )
                    // ------------------------

                    // Time In and Time Out side-by-side container
                    AttendanceActionCards(
                        lastTimeIn = lastTimeIn,
                        lastTimeOut = lastTimeOut,
                        isProcessingTimeIn = isProcTimeIn,
                        isProcessingTimeOut = isProcTimeOut,
                        onTimeInClick = {
                            if (lastTimeIn != null) {
                                viewModel.onTimeInClicked()
                            } else {
                                pendingBiometricIsTimeIn = true
                            }
                        },
                        onTimeOutClick = {
                            if (lastTimeOut != null) {
                                viewModel.onTimeOutClicked()
                            } else {
                                pendingBiometricIsTimeIn = false
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Detail container behind / below Time In & Time Out box:
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("employee_details_card"),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF131B2A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222F48))
                    ) {
                        val dividerColor = Color(0xFF222F48)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val displayName = if (employeeProfile.name.isNotBlank()) employeeProfile.name else "Employee ${employeeProfile.employeeCode.ifBlank { employeeProfile.employeeId }}"
                            LandingDetailRow(label = "Employee Name:", value = displayName)
                            HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                            val displayCode = employeeProfile.employeeCode.ifBlank { employeeProfile.employeeId.ifBlank { "000001" } }
                            LandingDetailRow(label = "Employee Code:", value = displayCode)
                            HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                            val displayCompany = employeeProfile.companyCode.ifBlank { "1001" }
                            LandingDetailRow(label = "Company Code:", value = displayCompany)
                            HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                            val rawLoc = (lastTimeIn?.locationName ?: lastTimeOut?.locationName ?: currentCoords.addressName ?: employeeProfile.location).trim()
                            val isCoordinateString = rawLoc.startsWith("Lat", ignoreCase = true) ||
                                    rawLoc.matches(Regex("^-?\\d+(\\.\\d+)?,\\s*-?\\d+(\\.\\d+)?$"))
                            val resolvedLocation = when {
                                rawLoc.isNotBlank() && !isCoordinateString -> rawLoc
                                employeeProfile.location.isNotBlank() && !employeeProfile.location.startsWith("Lat", ignoreCase = true) -> employeeProfile.location
                                else -> "Headquarters Office"
                            }
                            LandingDetailRow(label = "Location:", value = resolvedLocation)
                        }
                    }
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

    // Biometric Verification Sheet
    pendingBiometricIsTimeIn?.let { isTimeIn ->
        BiometricVerificationSheet(
            isTimeIn = isTimeIn,
            employeeName = employeeProfile.name.ifBlank { "VTP Employee" },
            employeeCode = employeeProfile.employeeCode.ifBlank { employeeProfile.employeeId.ifBlank { "001" } },
            imei = employeeProfile.imei,
            onHardwarePromptRequested = {
                val activity = BiometricAuthManager.findFragmentActivity(context)
                if (activity != null) {
                    BiometricAuthManager.promptBiometric(
                        activity = activity,
                        title = if (isTimeIn) "Verify Identity for Time In" else "Verify Identity for Time Out",
                        subtitle = "Employee #${employeeProfile.employeeCode.ifBlank { employeeProfile.employeeId }}",
                        onSuccess = {
                            val action = isTimeIn
                            pendingBiometricIsTimeIn = null
                            if (action) {
                                viewModel.onTimeInClicked()
                            } else {
                                viewModel.onTimeOutClicked()
                            }
                        },
                        onError = { _, errString ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Biometric authentication error: $errString")
                            }
                        },
                        onFailed = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Biometric recognition failed. Please try again.")
                            }
                        }
                    )
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Hardware biometric scanner not accessible in this context.")
                    }
                }
            },
            onVerificationSuccess = {
                val action = isTimeIn
                pendingBiometricIsTimeIn = null
                if (action) {
                    viewModel.onTimeInClicked()
                } else {
                    viewModel.onTimeOutClicked()
                }
            },
            onDismiss = {
                pendingBiometricIsTimeIn = null
            }
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

@Composable
private fun LandingDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Normal,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            fontSize = 13.5.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
