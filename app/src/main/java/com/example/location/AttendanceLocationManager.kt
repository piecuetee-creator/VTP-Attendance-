package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
    val isRealGps: Boolean = false,
    val accuracyMeters: Float = 0f
)

class AttendanceLocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Default simulation/headquarters coordinates (Karachi, Karim Chamber Offices from prompt screenshot)
    val defaultCoordinates = Coordinates(
        latitude = 24.8607,
        longitude = 67.0011,
        isRealGps = false,
        accuracyMeters = 5.0f
    )

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Coordinates {
        if (!hasLocationPermission()) {
            return defaultCoordinates
        }

        return try {
            suspendCancellableCoroutine { continuation ->
                val cts = CancellationTokenSource()

                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        continuation.resume(
                            Coordinates(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                isRealGps = true,
                                accuracyMeters = location.accuracy
                            )
                        )
                    } else {
                        // Try last known location fallback
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                            if (lastLoc != null) {
                                continuation.resume(
                                    Coordinates(
                                        latitude = lastLoc.latitude,
                                        longitude = lastLoc.longitude,
                                        isRealGps = true,
                                        accuracyMeters = lastLoc.accuracy
                                    )
                                )
                            } else {
                                continuation.resume(defaultCoordinates)
                            }
                        }.addOnFailureListener {
                            continuation.resume(defaultCoordinates)
                        }
                    }
                }.addOnFailureListener {
                    continuation.resume(defaultCoordinates)
                }

                continuation.invokeOnCancellation {
                    cts.cancel()
                }
            }
        } catch (_: Exception) {
            defaultCoordinates
        }
    }
}
