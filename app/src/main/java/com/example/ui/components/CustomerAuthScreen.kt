package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.location.AttendanceLocationManager
import com.example.model.EmployeeProfile
import com.example.ui.theme.VtpOrange
import com.example.ui.theme.VtpOrangeDark
import com.example.ui.theme.VtpOrangeGradient
import com.example.ui.theme.vtpTextFieldColors
import com.example.util.DeviceInfoManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Clean login and registration page with modern styling.
 * Shifted Name, Designation, and Location (auto-detect) from settings to here.
 * Fixed once entered, preventing in-app tampering.
 */
@Composable
fun CustomerAuthScreen(
    employeeProfile: EmployeeProfile,
    isAuthenticated: Boolean = false,
    onLoginSuccess: (companyCode: String, employeeCode: String, name: String, designation: String, location: String, serverDigits: String) -> Unit,
    onLogout: (() -> Unit)? = null,
    onNavigateToAttendance: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val locationManager = remember { AttendanceLocationManager(context) }

    var isSwitchingAccount by remember { mutableStateOf(false) }

    var companyCode by remember(employeeProfile.companyCode) {
        mutableStateOf(employeeProfile.companyCode.ifBlank { "1001" })
    }
    var employeeCode by remember(employeeProfile.employeeCode, employeeProfile.employeeId) {
        mutableStateOf(
            employeeProfile.employeeCode.ifBlank { employeeProfile.employeeId.ifBlank { "0452" } }
        )
    }
    var name by remember(employeeProfile.name) {
        mutableStateOf(employeeProfile.name)
    }
    var designation by remember(employeeProfile.designation) {
        mutableStateOf(employeeProfile.designation)
    }
    var location by remember(employeeProfile.location) {
        mutableStateOf(
            if (employeeProfile.location.isNotBlank() && !employeeProfile.location.startsWith("Lat", ignoreCase = true)) {
                employeeProfile.location
            } else {
                "Karim Chamber Offices, Karachi"
            }
        )
    }

    var isDetectingLocation by remember { mutableStateOf(false) }
    var locationDetectStatus by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            coroutineScope.launch {
                isDetectingLocation = true
                locationDetectStatus = "Detecting GPS location..."
                try {
                    val coords = locationManager.getCurrentLocation()
                    val addr = coords.addressName ?: coords.formatCoordinates()
                    location = addr
                    locationDetectStatus = "Auto-detected: $addr"
                } catch (e: Exception) {
                    locationDetectStatus = "Auto-detect failed, enter manually"
                } finally {
                    isDetectingLocation = false
                }
            }
        } else {
            locationDetectStatus = "Location permission needed for auto-detect"
        }
    }

    fun triggerAutoDetect() {
        if (locationManager.hasLocationPermission()) {
            coroutineScope.launch {
                isDetectingLocation = true
                locationDetectStatus = "Acquiring GPS location..."
                try {
                    val coords = locationManager.getCurrentLocation()
                    val addr = coords.addressName ?: coords.formatCoordinates()
                    location = addr
                    locationDetectStatus = "Location detected successfully"
                } catch (e: Exception) {
                    locationDetectStatus = "Auto-detect failed, please type location"
                } finally {
                    isDetectingLocation = false
                }
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Live preview of the 15-digit IMEI using fixed server digits from settings
    val fixedServerDigits = employeeProfile.serverDigits.ifBlank { "01" }
    val computedImei = remember(companyCode, employeeCode, fixedServerDigits) {
        DeviceInfoManager.buildVtpImei(companyCode, employeeCode, fixedServerDigits, context)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF0F0D0C)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF241D17),
                            Color(0xFF161310),
                            Color(0xFF0B0A09)
                        )
                    )
                )
        ) {
            // Close Option at Top-Right
            if (onClose != null) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 20.dp, end = 20.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .testTag("auth_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Login Dialog",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 40.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                            .size(76.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Brand Title Underneath Logo: "Presence"
                Text(
                    text = "Presence",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                // "Powered by VTP"
                Text(
                    text = "Powered by VTP",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VtpOrange,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Set up your profile once. Credentials will be locked permanently.",
                    fontSize = 12.5.sp,
                    color = Color(0xFFB8AEA5),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isAuthenticated && !isSwitchingAccount) {
                    AuthenticatedSessionCard(
                        name = employeeProfile.name,
                        designation = employeeProfile.designation,
                        location = employeeProfile.location,
                        companyCode = companyCode.ifBlank { employeeProfile.companyCode },
                        employeeCode = employeeCode.ifBlank { employeeProfile.employeeCode },
                        computedImei = computedImei,
                        onNavigateToAttendance = onNavigateToAttendance,
                        onSwitchAccount = {
                            isSwitchingAccount = true
                            employeeCode = ""
                            name = ""
                            designation = ""
                        },
                        onLogout = if (onLogout != null) {
                            {
                                onLogout()
                                isSwitchingAccount = true
                                employeeCode = ""
                                name = ""
                                designation = ""
                            }
                        } else null
                    )
                } else {
                    // Main Input Card with Gradient Glass Border
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(10.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black, spotColor = Color(0xFFFF6600).copy(alpha = 0.2f))
                            .clip(RoundedCornerShape(22.dp))
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color(0xFFFF6600).copy(alpha = 0.5f),
                                        Color.White.copy(alpha = 0.15f),
                                        Color(0xFFFF3D00).copy(alpha = 0.35f)
                                    )
                                ),
                                shape = RoundedCornerShape(22.dp)
                            ),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF181513).copy(alpha = 0.95f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Section Header: Fixed Profile Notice
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF231E1B))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = VtpOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Fixed Profile Setup • Locked after entry",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE2D9D2)
                                )
                            }

                            // 1. Company Code (strictly 4 digits)
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Company Code",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFF3ECE5)
                                    )
                                    Text(
                                        text = "${companyCode.length}/4 digits",
                                        fontSize = 11.sp,
                                        color = if (companyCode.length == 4) Color(0xFF10B981) else Color(0xFF888078),
                                        fontWeight = if (companyCode.length == 4) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedTextField(
                                    value = companyCode,
                                    onValueChange = { input ->
                                        val clean = input.filter { it.isDigit() }.take(4)
                                        companyCode = clean
                                        errorMessage = null
                                    },
                                    placeholder = { Text("Exact 4 digits (e.g. 1001)", color = Color(0xFF888078)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Business,
                                            contentDescription = null,
                                            tint = VtpOrange
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.NumberPassword,
                                        imeAction = ImeAction.Next
                                    ),
                                    colors = vtpTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_company_code")
                                )
                            }

                            // 2. Employee Code (strictly digits, 1 to 4 digits)
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Employee Code",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFF3ECE5)
                                    )
                                    Text(
                                        text = "${employeeCode.length}/4 digits",
                                        fontSize = 11.sp,
                                        color = if (employeeCode.isNotEmpty()) VtpOrange else Color(0xFF888078),
                                        fontWeight = if (employeeCode.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedTextField(
                                    value = employeeCode,
                                    onValueChange = { input ->
                                        val clean = input.filter { it.isDigit() }.take(4)
                                        employeeCode = clean
                                        errorMessage = null
                                    },
                                    placeholder = { Text("Up to 4 digits (e.g. 0452 or 0001)", color = Color(0xFF888078)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Badge,
                                            contentDescription = null,
                                            tint = VtpOrange
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.NumberPassword,
                                        imeAction = ImeAction.Next
                                    ),
                                    colors = vtpTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_employee_code")
                                )
                            }

                            // 3. Employee Full Name (Shifted from Settings)
                            Column {
                                Text(
                                    text = "Full Name",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFF3ECE5)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = {
                                        name = it
                                        errorMessage = null
                                    },
                                    placeholder = { Text("e.g. Saad Ali", color = Color(0xFF888078)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = VtpOrange
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    ),
                                    colors = vtpTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_full_name")
                                )
                            }

                            // 4. Designation / Job Title (Shifted from Settings)
                            Column {
                                Text(
                                    text = "Designation",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFF3ECE5)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = designation,
                                    onValueChange = {
                                        designation = it
                                        errorMessage = null
                                    },
                                    placeholder = { Text("e.g. Field Operations Lead", color = Color(0xFF888078)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Work,
                                            contentDescription = null,
                                            tint = VtpOrange
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    ),
                                    colors = vtpTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_designation")
                                )
                            }

                            // 5. Location (Auto-Detect + Editable, Shifted from Settings)
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Location (Auto-Detect)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFF3ECE5)
                                    )

                                    // Quick Auto Detect Trigger Button
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable(enabled = !isDetectingLocation) { triggerAutoDetect() }
                                            .background(VtpOrange.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isDetectingLocation) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(12.dp),
                                                color = VtpOrange,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Detecting...",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = VtpOrange
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.MyLocation,
                                                contentDescription = "Auto Detect Location",
                                                tint = VtpOrange,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Auto Detect",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = VtpOrange
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedTextField(
                                    value = location,
                                    onValueChange = {
                                        location = it
                                        errorMessage = null
                                    },
                                    placeholder = { Text("e.g. Karim Chamber Offices, Karachi", color = Color(0xFF888078)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = VtpOrange
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { triggerAutoDetect() },
                                            enabled = !isDetectingLocation
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MyLocation,
                                                contentDescription = "Auto Detect GPS Location",
                                                tint = if (isDetectingLocation) VtpOrange.copy(alpha = 0.5f) else VtpOrange
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    ),
                                    colors = vtpTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_location")
                                )

                                locationDetectStatus?.let { status ->
                                    Text(
                                        text = status,
                                        fontSize = 11.sp,
                                        color = if (status.contains("failed", ignoreCase = true)) Color(0xFFF87171) else Color(0xFF10B981),
                                        modifier = Modifier.padding(top = 3.dp, start = 4.dp)
                                    )
                                }
                            }

                            // Error message if any
                            AnimatedVisibility(visible = errorMessage != null) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = Color(0xFFF87171),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Radiant Gradient Login Action Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .scale(buttonScale)
                                    .shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = VtpOrange, spotColor = VtpOrange)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(VtpOrangeGradient)
                            ) {
                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        val cleanComp = companyCode.filter { it.isDigit() }
                                        val cleanEmp = employeeCode.filter { it.isDigit() }
                                        val cleanServer = fixedServerDigits.filter { it.isDigit() }.let {
                                            if (it.length > 2) it.takeLast(2) else it.padStart(2, '0')
                                        }.ifBlank { "01" }

                                        if (cleanComp.isBlank()) {
                                            errorMessage = "Please enter your 4-digit Company Code"
                                            return@Button
                                        }
                                        if (cleanComp.length != 4) {
                                            errorMessage = "Company Code must be exactly 4 digits (e.g. 1001)"
                                            return@Button
                                        }
                                        if (cleanEmp.isBlank()) {
                                            errorMessage = "Please enter your Employee Code (e.g. 0452)"
                                            return@Button
                                        }
                                        if (cleanEmp.length > 4) {
                                            errorMessage = "Employee Code cannot exceed 4 digits"
                                            return@Button
                                        }
                                        if (name.trim().isBlank()) {
                                            errorMessage = "Please enter your Full Name"
                                            return@Button
                                        }
                                        if (designation.trim().isBlank()) {
                                            errorMessage = "Please enter your Designation"
                                            return@Button
                                        }
                                        if (location.trim().isBlank()) {
                                            errorMessage = "Please enter or auto-detect your Location"
                                            return@Button
                                        }

                                        isAuthenticating = true
                                        errorMessage = null
                                        coroutineScope.launch {
                                            delay(350)
                                            isAuthenticating = false
                                            onLoginSuccess(
                                                cleanComp,
                                                cleanEmp,
                                                name.trim(),
                                                designation.trim(),
                                                location.trim(),
                                                cleanServer
                                            )
                                        }
                                    },
                                    enabled = !isAuthenticating,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("login_button"),
                                    interactionSource = interactionSource
                                ) {
                                    if (isAuthenticating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = Color.White,
                                            strokeWidth = 2.5.dp
                                        )
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Login,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Login to Attendance",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.2.sp
                                            )
                                        }
                                    }
                                }
                            }

                            if (isSwitchingAccount && isAuthenticated) {
                                Spacer(modifier = Modifier.height(4.dp))
                                TextButton(
                                    onClick = { isSwitchingAccount = false },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_cancel_switch_button")
                                ) {
                                    Text(
                                        text = "Cancel",
                                        color = Color(0xFFD4C8BE),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else if (onClose != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                TextButton(
                                    onClick = onClose,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_cancel_button")
                                ) {
                                    Text(
                                        text = "Close & Continue",
                                        color = Color(0xFFD4C8BE),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Minimal Footer Info
                Text(
                    text = "Presence • Smart Attendance System",
                    fontSize = 11.sp,
                    color = Color(0xFF787068)
                )
            }
        }
    }
}

@Composable
private fun AuthenticatedSessionCard(
    name: String,
    designation: String,
    location: String,
    companyCode: String,
    employeeCode: String,
    computedImei: String,
    onNavigateToAttendance: (() -> Unit)?,
    onSwitchAccount: () -> Unit,
    onLogout: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black, spotColor = Color(0xFF10B981).copy(alpha = 0.25f))
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(Color(0xFF10B981).copy(alpha = 0.6f), Color.White.copy(alpha = 0.15f), Color(0xFF10B981).copy(alpha = 0.4f))
                ),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1917).copy(alpha = 0.96f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Active Session Badge Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF064E3B).copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Active Session • Fixed & Authenticated",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD1FAE5)
                    )
                }
            }

            // Credentials Breakdown
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF12100E))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (name.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Employee Name", fontSize = 13.sp, color = Color(0xFFA69D95))
                        Text(
                            text = name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                if (designation.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Designation", fontSize = 13.sp, color = Color(0xFFA69D95))
                        Text(
                            text = designation,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFD4C8BE)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Company Code", fontSize = 13.sp, color = Color(0xFFA69D95))
                    Text(
                        text = companyCode,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Employee Code", fontSize = 13.sp, color = Color(0xFFA69D95))
                    Text(
                        text = employeeCode,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = VtpOrange
                    )
                }
                if (location.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fixed Location", fontSize = 13.sp, color = Color(0xFFA69D95))
                        Text(
                            text = location,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(0.6f)
                        )
                    }
                }
            }

            // Primary Action: Go to Attendance
            if (onNavigateToAttendance != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = VtpOrange, spotColor = VtpOrange)
                        .clip(RoundedCornerShape(14.dp))
                        .background(VtpOrangeGradient)
                ) {
                    Button(
                        onClick = onNavigateToAttendance,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("auth_go_to_attendance_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Go to Attendance",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Switch Account / Re-Login
            OutlinedButton(
                onClick = onSwitchAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("auth_switch_account_button"),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text(
                    text = "Edit Profile & Re-Login",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2D9D2)
                )
            }

            // Optional Full Logout
            if (onLogout != null) {
                TextButton(
                    onClick = onLogout,
                    modifier = Modifier.testTag("auth_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lock & Exit Session",
                        color = Color(0xFFF87171),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
