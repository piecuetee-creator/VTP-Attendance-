package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.EmployeeProfile
import com.example.ui.theme.VtpOrange
import com.example.ui.theme.VtpOrangeDark
import com.example.ui.theme.VtpOrangeGradient
import com.example.ui.theme.VtpOrangeLight
import com.example.ui.theme.vtpTextFieldColors
import com.example.util.DeviceInfoManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Clean login page with luxurious radiant gradient styling.
 * "User will enter only company and employee code. nothing else."
 * Automatically computes 15-digit IMEI: 99 + 02 + [company 4 digits] + [employee 4 digits] + [random 3 digits]
 */
@Composable
fun CustomerAuthScreen(
    employeeProfile: EmployeeProfile,
    onLoginSuccess: (companyCode: String, employeeCode: String) -> Unit,
    onClose: (() -> Unit)? = null,
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
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Radiant Gradient Logo Icon with Glow Ring
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF6600).copy(alpha = 0.15f))
                        .border(1.5.dp, Color(0xFFFF6600).copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFF3D00), Color(0xFFFF6600), Color(0xFFFFA040))
                                )
                            )
                            .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = VtpOrange, spotColor = VtpOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_vtp_presence_logo),
                            contentDescription = "Presence Logo",
                            modifier = Modifier
                                .size(66.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Brand Title Underneath Logo: "Presence"
                Text(
                    text = "Presence",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // "Powered by VTP"
                Text(
                    text = "Powered by VTP",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VtpOrange,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Enter your Company & Employee codes to authenticate",
                    fontSize = 13.sp,
                    color = Color(0xFFB8AEA5),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Main Input Card with Gradient Glass Border
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black, spotColor = Color(0xFFFF6600).copy(alpha = 0.2f))
                        .clip(RoundedCornerShape(22.dp))
                        .border(
                            width = 1.2.dp,
                            brush = Brush.linearGradient(
                                listOf(Color(0xFFFF7A00).copy(alpha = 0.4f), Color.White.copy(alpha = 0.15f), Color(0xFFFF7A00).copy(alpha = 0.25f))
                            ),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1916).copy(alpha = 0.95f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // 1. Company Code (xxxx)
                        Column {
                            Text(
                                text = "Company Code",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF3ECE5),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            OutlinedTextField(
                                value = companyCode,
                                onValueChange = { input ->
                                    val clean = input.filter { it.isDigit() }.take(4)
                                    companyCode = clean
                                    errorMessage = null
                                },
                                placeholder = { Text("4 digits (e.g. 1001)", color = Color(0xFF888078)) },
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
                                color = Color(0xFFF3ECE5),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            OutlinedTextField(
                                value = employeeCode,
                                onValueChange = { input ->
                                    val clean = input.filter { it.isDigit() }.take(4)
                                    employeeCode = clean
                                    errorMessage = null
                                },
                                placeholder = { Text("4 digits (e.g. 0452)", color = Color(0xFF888078)) },
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

                        // Real-time 15-Digit IMEI Breakdown Preview with Gradient Accent
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF13100E),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFF6600).copy(alpha = 0.35f), Color.White.copy(alpha = 0.1f))
                                )
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
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
                                            color = Color.White
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFFEA580C), Color(0xFFFF7A00))
                                                )
                                            )
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "9902 Pattern",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = computedImei,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFFF9E44),
                                    letterSpacing = 1.2.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "99 • 02 • ${companyCode.padStart(4, '0')} • ${employeeCode.padStart(4, '0')} • ${computedImei.takeLast(3)} (Random)",
                                    fontSize = 10.sp,
                                    color = Color(0xFFA69D95)
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
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

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

                        if (onClose != null) {
                            Spacer(modifier = Modifier.height(10.dp))
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

                Spacer(modifier = Modifier.height(24.dp))

                // Minimal Footer Info
                Text(
                    text = "Presence VTP • GT06 Biometric Protocol Client",
                    fontSize = 11.sp,
                    color = Color(0xFF787068)
                )
            }
        }
    }
}
