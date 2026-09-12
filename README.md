# VTP Attendance System (Android Client)

Enterprise Android application for employee presence and attendance tracking, built with Kotlin, Jetpack Compose, and Material Design 3.

## Overview

The VTP Attendance client provides an authenticated, biometric-verified attendance recording system with real-time GPS telemetry and GT06 protocol network streaming.

### Key Capabilities

- **Simplified Authentication**: Secure employee profile sign-in via 4-digit Company Code and up to 4-digit Employee Code.
- **Dynamic Terminal IMEI**: Generates and manages the 15-digit internal terminal IMEI with configurable protocol server routing.
- **Side-by-Side Action Cards**: Single-screen streamlined interface with **Time In** and **Time Out** primary actions.
- **Biometric Authentication**: Device credential and hardware fingerprint/face verification via Android `BiometricPrompt`.
- **Location Telemetry**: High-accuracy GPS positioning with mock-location detection and geofencing validation.
- **GT06 Protocol Integration**: Support for binary GT06 packets (`0x01` Login, `0x12` Location/Telemetry, `0x94` Information) over raw TCP sockets and WebSockets.
- **Offline Resiliency & Persistence**: Local Room database caching of attendance history and configuration parameters.

## Architecture

- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Design System**: High-contrast modern palette with warm accents (`#090D16` dark foundation, `#F97316` brand orange)
- **Architecture Pattern**: MVVM (Model-View-ViewModel) with Kotlin Coroutines and `StateFlow`
- **Data Layer**: Android Jetpack Room for persistent local storage
- **Networking**: Raw TCP Socket client with OkHttp WebSocket fallback

## Project Structure

```text
├── app/
│   ├── src/main/java/com/example/
│   │   ├── data/           # Repository and Room Database (AttendanceDao, etc.)
│   │   ├── location/       # Location tracking and GPS telemetry provider
│   │   ├── network/        # GT06 packet serializer and TCP/WebSocket client
│   │   ├── ui/             # Jetpack Compose UI screens and ViewModels
│   │   └── util/           # Device identifiers, IMEI manager, Biometrics
│   ├── src/main/res/       # Vector assets, themes, and string resources
│   └── build.gradle.kts    # Application Gradle configuration
├── gradle/                 # Gradle version catalog (libs.versions.toml) and wrapper
├── build.gradle.kts        # Root Gradle build script
└── settings.gradle.kts     # Project repository and module configuration
```

## Build & Run

### Prerequisites

- Android Studio Ladybug (2024.2) or newer
- JDK 17
- Android SDK with API 35/36 installed

### Building with Gradle

To assemble the debug APK from the command line:

```bash
./gradlew assembleDebug
```

The compiled APK will be generated at:
```text
app/build/outputs/apk/debug/app-debug.apk
```

### Running Tests

To run local unit and Robolectric tests:

```bash
./gradlew testDebugUnitTest
```
