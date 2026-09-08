package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmployeeProfile
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceCanvas
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VtpBlack
import com.example.ui.theme.VtpOrange
import com.example.ui.theme.VtpOrangeContainer
import com.example.ui.theme.VtpOrangeDark
import com.example.ui.theme.vtpTextFieldColors
import com.example.util.DeviceInfoManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Clean login page following user specification:
 * "User will enter only company and employee code. nothing else."
 * Automatically computes 15-digit IMEI: 99 + 02 + [company 4 digits] + [employee 4 digits] + [random 3 digits]
 */
@Composable
fun CustomerAuthScreen(
    employeeProfile: EmployeeProfile,
    onLoginSuccess: (companyCode: String, employeeCode: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var companyCode by remember {
        mutableStateOf(
            employeeProfile.companyCode.ifBlank { "1001" }
        )
    }
    var employeeCode by remember {
        mutableStateOf(
            employeeProfile.employeeCode.ifBlank { employeeProfile.employeeId.ifBlank { "0452" } }
        )
    }
    var isAuthenticating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Live preview of the 15-digit IMEI following formula: 9902xxxxxxxxxxx
    val computedImei = remember(companyCode, employeeCode) {
        DeviceInfoManager.buildVtpImei(companyCode, employeeCode, context)
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
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand Logo / Icon in Orange, Black & White
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(VtpOrange)
                    .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = VtpOrange, spotColor = VtpOrange),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = "Presence Logo",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Presence Attendance",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Sign in with your Company & Employee codes",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Input Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // 1. Company Code (xxxx)
                    Column {
                        Text(
                            text = "Company Code",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = companyCode,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }.take(4)
                                companyCode = clean
                                errorMessage = null
                            },
                            placeholder = { Text("4 digits (e.g. 1001)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = VtpOrange
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            colors = vtpTextFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_company_code")
                        )
                    }

                    // 2. Employee Code (xxxx)
                    Column {
                        Text(
                            text = "Employee Code",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = employeeCode,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }.take(4)
                                employeeCode = clean
                                errorMessage = null
                            },
                            placeholder = { Text("4 digits (e.g. 0452)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = VtpOrange
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
                            colors = vtpTextFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_employee_code")
                        )
                    }

                    // Real-time 15-Digit IMEI Breakdown Preview
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
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
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "15-Digit Terminal IMEI",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(VtpOrangeContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "9902 Pattern",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VtpOrangeDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = computedImei,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = VtpOrange,
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "99 (Fix) • 02 (Fix) • ${companyCode.padStart(4, '0')} (Company) • ${employeeCode.padStart(4, '0')} (Employee) • ${computedImei.takeLast(3)} (Rand)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Error message if any
                    AnimatedVisibility(visible = errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Login Action Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (companyCode.isBlank()) {
                                errorMessage = "Please enter your 4-digit Company Code"
                                return@Button
                            }
                            if (employeeCode.isBlank()) {
                                errorMessage = "Please enter your 4-digit Employee Code"
                                return@Button
                            }

                            isAuthenticating = true
                            errorMessage = null
                            coroutineScope.launch {
                                delay(400)
                                isAuthenticating = false
                                onLoginSuccess(companyCode, employeeCode)
                            }
                        },
                        enabled = !isAuthenticating,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VtpOrange,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .scale(buttonScale)
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
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Minimal Footer Info
            Text(
                text = "Presence VTP • GT06 Biometric Protocol Client",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
