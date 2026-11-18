import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.vtp.attendance"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      // Standard debug configuration
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.biometric)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // ksp(libs.androidx.room.compiler)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.firebase.appcheck.debug)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

abstract class CopyApkTask : DefaultTask() {
  @get:InputDirectory
  abstract val sourceDir: DirectoryProperty

  @get:OutputDirectory
  abstract val targetDir: DirectoryProperty

  @get:Internal
  abstract val sdkRootPath: Property<String>

  @get:Internal
  abstract val rootApkFile: RegularFileProperty

  @TaskAction
  fun copyApks() {
    val src = sourceDir.get().asFile
    val dst = targetDir.get().asFile
    if (!dst.exists()) {
      dst.mkdirs()
    }
    dst.listFiles()?.filter { it.name.endsWith(".idsig") }?.forEach { it.delete() }

    val debugApk = File(src, "app-debug.apk")
    if (!debugApk.exists()) return

    val outAppDebug = File(dst, "app-debug.apk")
    val outVtpApk = File(dst, "vtp-attendance.apk")
    val outRootApk = rootApkFile.get().asFile

    val sdkDir = File(sdkRootPath.get())
    val buildToolsDir = File(sdkDir, "build-tools")
      .listFiles { f -> f.isDirectory }
      ?.sortedBy { it.name }
      ?.lastOrNull()
    val zipalign = buildToolsDir?.let { File(it, "zipalign") }?.takeIf { it.canExecute() }
    val apksigner = buildToolsDir?.let { File(it, "apksigner") }?.takeIf { it.canExecute() }
    val debugKeystore = File(System.getProperty("user.home"), ".android/debug.keystore")
      .takeIf { it.exists() }

    if (zipalign != null && apksigner != null && debugKeystore != null) {
      val alignedTmp = File(dst, "app-debug-aligned.tmp.apk")
      try {
        val alignProc = ProcessBuilder(
          zipalign.absolutePath, "-f", "-p", "-P", "16", "4",
          debugApk.absolutePath, alignedTmp.absolutePath
        ).redirectErrorStream(true).start()
        val alignExit = alignProc.waitFor()
        val inputForSign = if (alignExit == 0 && alignedTmp.exists()) alignedTmp else debugApk

        val signProc = ProcessBuilder(
          apksigner.absolutePath, "sign",
          "--ks", debugKeystore.absolutePath,
          "--ks-pass", "pass:android",
          "--ks-key-alias", "androiddebugkey",
          "--key-pass", "pass:android",
          "--min-sdk-version", "21",
          "--v1-signing-enabled", "true",
          "--v2-signing-enabled", "true",
          "--v3-signing-enabled", "true",
          "--v4-signing-enabled", "false",
          "--out", outAppDebug.absolutePath,
          inputForSign.absolutePath
        ).redirectErrorStream(true).start()
        val signOutput = signProc.inputStream.bufferedReader().readText()
        val signExit = signProc.waitFor()
        if (signExit != 0) {
          throw GradleException("apksigner failed ($signExit): $signOutput")
        }
      } finally {
        alignedTmp.delete()
        dst.listFiles()?.filter { it.name.endsWith(".idsig") }?.forEach { it.delete() }
      }
    } else {
      debugApk.copyTo(outAppDebug, overwrite = true)
    }

    outAppDebug.copyTo(outVtpApk, overwrite = true)
    outAppDebug.copyTo(outRootApk, overwrite = true)
  }
}

val copyApkTask = tasks.register<CopyApkTask>("copyApkToFolder") {
  sourceDir.set(layout.buildDirectory.dir("outputs/apk/debug"))
  targetDir.set(File(rootDir, "apk"))
  sdkRootPath.set(
    providers.environmentVariable("ANDROID_SDK_ROOT")
      .orElse(providers.environmentVariable("ANDROID_HOME"))
      .orElse("")
  )
  rootApkFile.set(File(rootDir, "vtp-attendance.apk"))
  outputs.upToDateWhen { false }
}

tasks.matching { it.name == "assembleDebug" }.configureEach {
  finalizedBy(copyApkTask)
}
