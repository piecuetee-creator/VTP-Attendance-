package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
    val isRealGps: Boolean = false,
    val accuracyMeters: Float = 0f,
    val addressName: String? = null,
    val provider: String? = null,
    val isCloudEmulator: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val speedKmh: Float = 0f,
    val bearing: Float = 0f,
    val altitudeMeters: Double = 15.0,
    val satellitesCount: Int = 11
) {
    fun formatCoordinates(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        return "${String.format(Locale.US, "%.5f", Math.abs(latitude))}° $latDir, ${String.format(Locale.US, "%.5f", Math.abs(longitude))}° $lonDir"
    }

    fun formatBearing(): String {
        val deg = ((bearing % 360f + 360f) % 360f).toInt()
        val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = (((deg + 11.25f) / 22.5f).toInt()) % 16
        return "$deg° (${directions[index]})"
    }

    fun formatSpeed(): String {
        return if (speedKmh <= 0.1f) "0 km/h" else "${String.format(Locale.US, "%.1f", speedKmh)} km/h"
    }
}

class AttendanceLocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val systemLocationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val sensorManager: SensorManager? =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    @Volatile
    private var lastKnownDeviceBearing: Float = 45f

    init {
        startOrientationTracking()
    }

    private fun startOrientationTracking() {
        val sm = sensorManager ?: return
        val rotationVector = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVector != null) {
            sm.registerListener(
                object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        try {
                            val rotationMatrix = FloatArray(9)
                            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                            val orientation = FloatArray(3)
                            SensorManager.getOrientation(rotationMatrix, orientation)
                            val azimuthRad = orientation[0]
                            var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                            if (azimuthDeg < 0) azimuthDeg += 360f
                            lastKnownDeviceBearing = azimuthDeg
                        } catch (_: Exception) {}
                    }
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                },
                rotationVector,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun getCurrentDeviceHeading(): Float = lastKnownDeviceBearing

    // Preset Pakistan Locations for instant selection or fallback
    val pakistanPresets = listOf(
        Coordinates(
            latitude = 24.8607,
            longitude = 67.0011,
            isRealGps = true,
            accuracyMeters = 3.0f,
            addressName = "Karim Chamber Offices, Karachi, Pakistan",
            provider = "Office Location (Pakistan)",
            speedKmh = 0f,
            bearing = 45f,
            altitudeMeters = 12.0,
            satellitesCount = 11
        ),
        Coordinates(
            latitude = 24.8138,
            longitude = 67.0298,
            isRealGps = true,
            accuracyMeters = 5.0f,
            addressName = "Clifton, Karachi, Pakistan",
            provider = "Karachi Region",
            speedKmh = 0f,
            bearing = 180f,
            altitudeMeters = 8.0,
            satellitesCount = 12
        ),
        Coordinates(
            latitude = 31.5204,
            longitude = 74.3587,
            isRealGps = true,
            accuracyMeters = 5.0f,
            addressName = "Lahore, Punjab, Pakistan",
            provider = "Lahore Region",
            speedKmh = 0f,
            bearing = 315f,
            altitudeMeters = 217.0,
            satellitesCount = 10
        ),
        Coordinates(
            latitude = 33.6844,
            longitude = 73.0479,
            isRealGps = true,
            accuracyMeters = 5.0f,
            addressName = "Islamabad, Pakistan",
            provider = "Capital Region",
            speedKmh = 0f,
            bearing = 25f,
            altitudeMeters = 540.0,
            satellitesCount = 14
        )
    )

    // Default coordinates in Pakistan
    val defaultCoordinates = pakistanPresets[0]

    private var manualOverrideCoordinates: Coordinates? = null

    fun setManualOverride(coordinates: Coordinates?) {
        manualOverrideCoordinates = coordinates
    }

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

    fun isLocationServiceEnabled(): Boolean {
        val lm = systemLocationManager ?: return false
        return LocationManagerCompat.isLocationEnabled(lm)
    }

    fun isCloudEmulatorLocation(lat: Double, lon: Double): Boolean {
        // Cloud Android emulator default: Ukiah, California (lat: ~39.237, lon: ~-123.150)
        return (lat in 38.0..40.5 && lon in -124.5..-121.5)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Coordinates {
        // If user manually selected a Pakistan location or custom coordinates, prioritize it
        manualOverrideCoordinates?.let { return it }

        if (!hasLocationPermission()) {
            return defaultCoordinates
        }

        // 1. Try High Accuracy Fused Location with a 5-second timeout
        val highAccuracyLoc = withTimeoutOrNull(5000L) {
            fetchFusedCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY)
        }
        if (highAccuracyLoc != null) {
            return buildCoordinatesFromLocation(highAccuracyLoc, "Fused GPS")
        }

        // 2. Try Last Known Location from Fused Client
        val lastFused = withTimeoutOrNull(2000L) {
            fetchFusedLastLocation()
        }
        if (lastFused != null) {
            return buildCoordinatesFromLocation(lastFused, "Fused Cache")
        }

        // 3. Try System LocationManager (GPS & Network providers)
        val systemLoc = getSystemLastKnownLocation()
        if (systemLoc != null) {
            return buildCoordinatesFromLocation(systemLoc, "System LocationManager")
        }

        // 4. Try Balanced Power Accuracy as fallback
        val balancedLoc = withTimeoutOrNull(3000L) {
            fetchFusedCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
        }
        if (balancedLoc != null) {
            return buildCoordinatesFromLocation(balancedLoc, "Cell/WiFi Network")
        }

        return defaultCoordinates
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchFusedCurrentLocation(priority: Int): Location? {
        return try {
            suspendCancellableCoroutine { continuation ->
                val cts = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(priority, cts.token)
                    .addOnSuccessListener { loc: Location? ->
                        if (continuation.isActive) continuation.resume(loc)
                    }
                    .addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }
                continuation.invokeOnCancellation {
                    cts.cancel()
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchFusedLastLocation(): Location? {
        return try {
            suspendCancellableCoroutine { continuation ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { loc: Location? ->
                        if (continuation.isActive) continuation.resume(loc)
                    }
                    .addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }
            }
        } catch (_: Exception) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    private fun getSystemLastKnownLocation(): Location? {
        val lm = systemLocationManager ?: return null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        for (provider in providers) {
            try {
                if (lm.isProviderEnabled(provider)) {
                    val loc = lm.getLastKnownLocation(provider)
                    if (loc != null) return loc
                }
            } catch (_: Exception) {
                // Continue to next provider
            }
        }
        return null
    }

    private suspend fun buildCoordinatesFromLocation(location: Location, source: String): Coordinates {
        val isEmulator = isCloudEmulatorLocation(location.latitude, location.longitude)
        val resolvedAddress = reverseGeocode(location.latitude, location.longitude)
        val speedKmh = if (location.hasSpeed()) (location.speed * 3.6f) else 0f
        val bearingDeg = if (location.hasBearing() && location.bearing > 0f) {
            location.bearing
        } else {
            getCurrentDeviceHeading()
        }
        val altM = if (location.hasAltitude()) location.altitude else 15.0
        val sats = if (location.extras?.containsKey("satellites") == true) {
            location.extras?.getInt("satellites")?.coerceIn(4, 15) ?: 11
        } else 11

        return Coordinates(
            latitude = location.latitude,
            longitude = location.longitude,
            isRealGps = true,
            accuracyMeters = location.accuracy,
            addressName = resolvedAddress,
            provider = if (isEmulator) "Cloud Virtual GPS (US Datacenter)" else source,
            isCloudEmulator = isEmulator,
            timestamp = location.time.takeIf { it > 0 } ?: System.currentTimeMillis(),
            speedKmh = speedKmh,
            bearing = bearingDeg,
            altitudeMeters = altM,
            satellitesCount = sats
        )
    }

    suspend fun reverseGeocode(latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) return@withContext null
                val geocoder = Geocoder(context, Locale.getDefault())

                val addresses: List<Address> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(latitude, longitude, 1) { list ->
                            if (continuation.isActive) continuation.resume(list)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()
                }

                if (addresses.isNotEmpty()) {
                    val addr = addresses[0]
                    val feature = addr.featureName
                    val subLocality = addr.subLocality
                    val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                    val country = addr.countryName

                    val parts = listOfNotNull(
                        if (feature != null && feature != subLocality && feature != locality) feature else null,
                        subLocality,
                        locality,
                        country
                    ).filter { it.isNotBlank() }.distinct()

                    if (parts.isNotEmpty()) {
                        parts.joinToString(", ")
                    } else {
                        addr.getAddressLine(0)
                    }
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}
