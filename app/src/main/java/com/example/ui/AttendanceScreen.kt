package com.example.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AttendanceActionCards
import com.example.ui.components.AttendanceHeader
import com.example.ui.components.AttendanceLocationCard
import com.example.ui.components.AttendanceSecondaryActions
import com.example.ui.components.AttendanceSuccessDialog
import com.example.ui.components.ConsoleLogSheet
import com.example.ui.components.CustomerAuthScreen
import com.example.ui.components.SettingsSheet
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DIBEmeraldContainer
import com.example.ui.theme.DIBEmeraldDark
import com.example.ui.theme.DIBEmeraldPrimary
import com.example.ui.theme.DIBGoldAccent
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
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

    var showConsoleSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
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

    if (!authState.isAuthenticated) {
        CustomerAuthScreen(
            employeeProfile = employeeProfile,
            onLoginSuccess = { viewModel.loginSuccess() },
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
            AttendanceHeader(
                connectionStatus = connectionStatus,
                onOpenTerminal = { showConsoleSheet = true },
                onOpenSettings = { showSettingsSheet = true },
                onLock = { viewModel.logout() }
            )
        },
        containerColor = SurfaceCanvas
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Live Date & Time Status Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = DIBEmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentTimeString.ifEmpty { "Loading time..." },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(DIBGoldAccent)
                    )
                }
            }

            // Employee Welcome Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(DIBEmeraldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Employee Avatar",
                            tint = DIBEmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = employeeProfile.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ID: ${employeeProfile.employeeId}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DIBEmeraldPrimary
                            )
                            Text(
                                text = " • ",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = employeeProfile.designation,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Attendance Action Cards: Time In & Time Out
            AttendanceActionCards(
                lastTimeIn = lastTimeIn,
                lastTimeOut = lastTimeOut,
                isProcessingTimeIn = isProcTimeIn,
                isProcessingTimeOut = isProcTimeOut,
                onTimeInClick = { viewModel.onTimeInClicked() },
                onTimeOutClick = { viewModel.onTimeOutClicked() }
            )

            // Location Card
            AttendanceLocationCard(
                coordinates = currentCoords,
                locationName = employeeProfile.location,
                isLoadingLocation = isLoadingLoc,
                onRefreshLocation = { viewModel.refreshLocation() }
            )

            // Reset Biometric & Console Links
            AttendanceSecondaryActions(
                onResetBiometric = {
                    viewModel.resetBiometric()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Biometric attendance cache cleared for today.")
                    }
                },
                onOpenConsole = { showConsoleSheet = true },
                onLockSession = { viewModel.logout() }
            )
        }
    }

    // Success Confirmation Dialog (from Screenshot 2)
    activeDialogRecord?.let { record ->
        AttendanceSuccessDialog(
            record = record,
            profile = employeeProfile,
            isAlreadyMarked = isDialogAlreadyMarked,
            onDismiss = { viewModel.dismissDialog() }
        )
    }

    // Live Socket Console Sheet
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

    // Settings & Profile Sheet
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
