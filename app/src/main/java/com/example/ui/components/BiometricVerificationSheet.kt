package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VtpBlack
import com.example.ui.theme.VtpOrange
import com.example.ui.theme.VtpOrangeDark
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class BiometricVerifyState {
    READY,
    SCANNING,
    SUCCESS,
    FAILED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricVerificationSheet(
    isTimeIn: Boolean,
    employeeName: String,
    employeeCode: String,
    imei: String,
    onHardwarePromptRequested: () -> Unit,
    onVerificationSuccess: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var verifyState by remember { mutableStateOf(BiometricVerifyState.READY) }
    var statusText by remember {
        mutableStateOf("Hold sensor or tap Hardware Scanner to verify")
    }
    var scanProgress by remember { mutableStateOf(0f) }
    val animatedProgress = remember { Animatable(0f) }
    var holdJob by remember { mutableStateOf<Job?>(null) }

    // Pulsing aura animation for sensor scanner
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    ModalBottomSheet(
        onDismissRequest = {
            if (verifyState != BiometricVerifyState.SCANNING) {
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = Color(0xFF141210),
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
            )
        },
        modifier = modifier.testTag("biometric_verification_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Security Badge Pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isTimeIn) VtpOrange.copy(alpha = 0.15f) else Color(0xFF27272A),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isTimeIn) VtpOrange.copy(alpha = 0.4f) else Color(0xFF52525B)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (isTimeIn) VtpOrange else Color(0xFFA1A1AA),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTimeIn) "Time In Verification" else "Time Out Verification",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTimeIn) VtpOrange else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Biometric Identity Check",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-0.3).sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Employee #$employeeCode • $employeeName",
                fontSize = 13.5.sp,
                color = Color(0xFFC7BCB3),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(28.dp))

            // THE BIOMETRIC SENSOR TARGET: Supports both Touch-and-Hold & Hardware BiometricPrompt
            Box(
                modifier = Modifier
                    .size(170.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Pulsing Glow Aura
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(if (verifyState == BiometricVerifyState.SCANNING) 1.15f else pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    when (verifyState) {
                                        BiometricVerifyState.SUCCESS -> Color(0xFF10B981).copy(alpha = 0.35f)
                                        BiometricVerifyState.FAILED -> Color(0xFFEF4444).copy(alpha = 0.35f)
                                        BiometricVerifyState.SCANNING -> VtpOrange.copy(alpha = 0.4f)
                                        else -> VtpOrange.copy(alpha = 0.18f)
                                    },
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Circular Progress Indicator during touch & hold
                if (verifyState == BiometricVerifyState.SCANNING) {
                    CircularProgressIndicator(
                        progress = { animatedProgress.value },
                        modifier = Modifier.size(142.dp),
                        color = VtpOrange,
                        trackColor = Color.White.copy(alpha = 0.12f),
                        strokeWidth = 4.dp
                    )
                }

                // Interactive Biometric Sensor Touch Surface
                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            ambientColor = if (verifyState == BiometricVerifyState.SUCCESS) Color(0xFF10B981) else VtpOrange,
                            spotColor = if (verifyState == BiometricVerifyState.SUCCESS) Color(0xFF10B981) else VtpOrange
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                when (verifyState) {
                                    BiometricVerifyState.SUCCESS -> listOf(Color(0xFF059669), Color(0xFF10B981))
                                    BiometricVerifyState.FAILED -> listOf(Color(0xFFDC2626), Color(0xFFEF4444))
                                    BiometricVerifyState.SCANNING -> listOf(Color(0xFFEA580C), Color(0xFFFF7A00))
                                    else -> listOf(Color(0xFF24201D), Color(0xFF181513))
                                }
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                when (verifyState) {
                                    BiometricVerifyState.SUCCESS -> listOf(Color(0xFF34D399), Color(0xFF10B981))
                                    BiometricVerifyState.FAILED -> listOf(Color(0xFFFCA5A5), Color(0xFFEF4444))
                                    BiometricVerifyState.SCANNING -> listOf(Color(0xFFFFB266), Color(0xFFFF6600))
                                    else -> listOf(Color(0xFFFF6600).copy(alpha = 0.7f), Color.White.copy(alpha = 0.2f))
                                }
                            ),
                            shape = CircleShape
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    if (verifyState == BiometricVerifyState.SUCCESS) return@detectTapGestures

                                    verifyState = BiometricVerifyState.SCANNING
                                    statusText = "Scanning fingerprint... Hold steady"

                                    holdJob = coroutineScope.launch {
                                        animatedProgress.snapTo(0f)
                                        animatedProgress.animateTo(
                                            targetValue = 1f,
                                            animationSpec = tween(durationMillis = 1400, easing = LinearEasing)
                                        )
                                        // Once complete
                                        verifyState = BiometricVerifyState.SUCCESS
                                        statusText = "Identity Verified Successfully!"
                                        delay(450)
                                        onVerificationSuccess()
                                    }

                                    tryAwaitRelease()

                                    // If released before 100% completion
                                    if (animatedProgress.value < 0.98f && verifyState == BiometricVerifyState.SCANNING) {
                                        holdJob?.cancel()
                                        coroutineScope.launch {
                                            animatedProgress.snapTo(0f)
                                            verifyState = BiometricVerifyState.READY
                                            statusText = "Finger lifted too early. Touch and hold firmly."
                                        }
                                    }
                                }
                            )
                        }
                        .testTag("biometric_sensor_surface"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (verifyState) {
                            BiometricVerifyState.SUCCESS -> Icons.Default.CheckCircle
                            else -> Icons.Default.Fingerprint
                        },
                        contentDescription = "Biometric Sensor",
                        tint = when (verifyState) {
                            BiometricVerifyState.SUCCESS -> Color.White
                            BiometricVerifyState.FAILED -> Color.White
                            BiometricVerifyState.SCANNING -> Color.White
                            else -> VtpOrange
                        },
                        modifier = Modifier.size(62.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Live State Description
            Text(
                text = statusText,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = when (verifyState) {
                    BiometricVerifyState.SUCCESS -> Color(0xFF34D399)
                    BiometricVerifyState.FAILED -> Color(0xFFEF4444)
                    BiometricVerifyState.SCANNING -> VtpOrange
                    else -> Color(0xFFF3ECE5)
                },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Hold your finger on the sensor or use your device's system biometric prompt below",
                fontSize = 12.sp,
                color = Color(0xFF948A80),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Hardware Scanner Action Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1F1C19),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38322D))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(VtpOrange.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = VtpOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "System Biometrics",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Fingerprint / Face / Device PIN",
                                fontSize = 11.5.sp,
                                color = Color(0xFFA89F95)
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            onHardwarePromptRequested()
                        },
                        modifier = Modifier.testTag("use_hardware_biometric_button")
                    ) {
                        Text(
                            text = "Open Prompt",
                            fontWeight = FontWeight.Bold,
                            color = VtpOrange,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cancel action
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_biometric_button")
            ) {
                Text(
                    text = "Cancel",
                    color = Color(0xFFA89F95),
                    fontSize = 14.sp
                )
            }
        }
    }
}
