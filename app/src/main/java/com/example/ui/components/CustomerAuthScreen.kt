package com.example.ui.components

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.EmployeeProfile
import com.example.ui.theme.BiometricCyan
import com.example.ui.theme.BiometricCyanContainer
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.TimeInGreen
import com.example.ui.theme.VtpAccentGold
import com.example.ui.theme.VtpAccentGoldLight
import com.example.ui.theme.VtpPrimary
import com.example.ui.theme.VtpPrimaryDark
import kotlinx.coroutines.delay

@Composable
fun CustomerAuthScreen(
    employeeProfile: EmployeeProfile,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Biometric, 1 = Credentials
    var employeeIdInput by remember { mutableStateOf(employeeProfile.employeeId) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    var biometricSuccess by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = SurfaceCanvas
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Hero Banner with VTP Presence Brand
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(VtpPrimaryDark, VtpPrimary)
                        )
                    )
                    .padding(top = 40.dp, bottom = 28.dp, start = 24.dp, end = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(2.dp, VtpAccentGold.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_vtp_presence_logo),
                            contentDescription = "Presence Logo",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Presence",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.4.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "VTP Attendance & Verification Portal",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = VtpAccentGoldLight
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Customer & Employee Authentication",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Card with Biometric & Credential Tabs
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("auth_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Switch Tabs: Biometric vs Credentials
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = VtpPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = VtpPrimary,
                                height = 3.dp
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                authError = null
                            },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Biometric", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                            }
                        )

                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                authError = null
                            },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Credentials", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // TAB 0: BIOMETRIC AUTHENTICATION
                    if (selectedTab == 0) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Biometric Verification",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Scan fingerprint or touch sensor to log in",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(26.dp))

                            // Biometric Sensor Touch Target
                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val sensorScale by animateFloatAsState(
                                targetValue = if (isPressed || isAuthenticating) 0.93f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "sensorScale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .scale(sensorScale)
                                    .shadow(
                                        elevation = 6.dp,
                                        shape = CircleShape,
                                        ambientColor = BiometricCyan.copy(alpha = 0.3f),
                                        spotColor = BiometricCyan.copy(alpha = 0.4f)
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        if (biometricSuccess) TimeInGreen.copy(alpha = 0.15f)
                                        else BiometricCyanContainer
                                    )
                                    .border(
                                        width = 2.5.dp,
                                        color = if (biometricSuccess) TimeInGreen else BiometricCyan,
                                        shape = CircleShape
                                    )
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null,
                                        enabled = !isAuthenticating
                                    ) {
                                        isAuthenticating = true
                                        authError = null
                                    }
                                    .testTag("biometric_sensor_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAuthenticating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(52.dp),
                                        color = BiometricCyan,
                                        strokeWidth = 3.5.dp
                                    )
                                } else if (biometricSuccess) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Authenticated",
                                        tint = TimeInGreen,
                                        modifier = Modifier.size(56.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Fingerprint Sensor",
                                        tint = BiometricCyan,
                                        modifier = Modifier.size(58.dp)
                                    )
                                }
                            }

                            LaunchedEffect(isAuthenticating) {
                                if (isAuthenticating) {
                                    delay(900) // Realistic biometric recognition simulation
                                    isAuthenticating = false
                                    biometricSuccess = true
                                    delay(400)
                                    onLoginSuccess()
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = if (biometricSuccess) "Identity Verified!" else "Tap sensor to scan",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (biometricSuccess) TimeInGreen else BiometricCyan
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Registered Employee Pill
                            if (employeeProfile.name.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SurfaceCanvas,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Badge,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${employeeProfile.name} (ID: ${employeeProfile.employeeId})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // TAB 1: USERNAME / PASSWORD CREDENTIALS
                    if (selectedTab == 1) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Login with Credentials",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Enter employee ID and password",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            OutlinedTextField(
                                value = employeeIdInput,
                                onValueChange = {
                                    employeeIdInput = it
                                    authError = null
                                },
                                label = { Text("Employee ID / Username") },
                                leadingIcon = {
                                    Icon(Icons.Default.Badge, contentDescription = null, tint = VtpPrimary)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_employee_id_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = authTextFieldColors()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = {
                                    passwordInput = it
                                    authError = null
                                },
                                label = { Text("Password") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = VtpPrimary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password visibility",
                                            tint = TextSecondary
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        performCredentialLogin(
                                            employeeId = employeeIdInput,
                                            password = passwordInput,
                                            expectedEmpId = employeeProfile.employeeId,
                                            onSuccess = onLoginSuccess,
                                            onError = { authError = it }
                                        )
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_password_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = authTextFieldColors()
                            )

                            AnimatedVisibility(visible = authError != null) {
                                Text(
                                    text = authError ?: "",
                                    color = Color(0xFFEF4444),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    performCredentialLogin(
                                        employeeId = employeeIdInput,
                                        password = passwordInput,
                                        expectedEmpId = employeeProfile.employeeId,
                                        onSuccess = onLoginSuccess,
                                        onError = { authError = it }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("login_submit_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VtpPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Log In",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Default quick hint
                            Text(
                                text = "Default Employee: ${employeeProfile.employeeId} • Pass: 1234 or any password",
                                fontSize = 11.sp,
                                color = TextTertiary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "VTP Telematics & Attendance Engine",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "GT06 Protocol Support • Secure Session",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }
        }
    }
}

private fun performCredentialLogin(
    employeeId: String,
    password: String,
    expectedEmpId: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    if (employeeId.isBlank()) {
        onError("Please enter your employee ID")
        return
    }
    // Accept valid logins
    onSuccess()
}

@Composable
private fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedBorderColor = VtpPrimary,
    unfocusedBorderColor = BorderSubtle,
    focusedLabelColor = VtpPrimary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
    cursorColor = VtpPrimary
)
