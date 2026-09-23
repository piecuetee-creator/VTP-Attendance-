package com.example.util

import android.content.Context
import android.net.Uri
import com.example.model.SocketConfig
import org.json.JSONObject
import java.io.File

object JsonConfigManager {

    data class AppJsonConfig(
        val socketConfig: SocketConfig,
        val companyName: String? = null,
        val hideSettingsUi: Boolean = false,
        val companyCode: String? = null,
        val employeeCode: String? = null,
        val serverDigits: String? = null
    )

    /**
     * Returns a pristine sample JSON template for Farhan Bhai / demo setups.
     */
    fun getSampleConfigJson(): String {
        return """{
  "server": {
    "tcp_host": "avl.vtps.org",
    "tcp_port": 5200,
    "ws_url": "ws://avl.vtps.org:5200",
    "use_websocket": true
  },
  "protocol": {
    "server_digits": "01",
    "timezone_offset_hours": -5
  },
  "app": {
    "company_name": "Demo Tracking Co",
    "hide_settings_ui": true
  }
}""".trimIndent()
    }

    /**
     * Parses a JSON configuration string into [AppJsonConfig].
     * Supports both nested objects ("server", "protocol", "app") and flat keys.
     */
    fun parseJsonConfig(jsonStr: String, currentConfig: SocketConfig): AppJsonConfig {
        val root = JSONObject(jsonStr.trim())

        // 1. Server block or top-level keys
        val serverObj = root.optJSONObject("server")
        val tcpHost = serverObj?.optString("tcp_host")
            ?: root.optString("tcp_host", root.optString("host", currentConfig.tcpHost))
        val tcpPort = serverObj?.optInt("tcp_port", -1)?.takeIf { it > 0 }
            ?: root.optInt("tcp_port", root.optInt("port", currentConfig.tcpPort))
        val wsUrl = serverObj?.optString("ws_url")
            ?: root.optString("ws_url", root.optString("url", currentConfig.wsUrl))
        val useWs = when {
            serverObj?.has("use_websocket") == true -> serverObj.getBoolean("use_websocket")
            root.has("use_websocket") -> root.getBoolean("use_websocket")
            else -> currentConfig.useWebSocket
        }

        // 2. Protocol block
        val protocolObj = root.optJSONObject("protocol")
        val serverDigits = protocolObj?.optString("server_digits")
            ?: root.optString("server_digits", currentConfig.serverDigits)
        val tzOffset = when {
            protocolObj?.has("timezone_offset_hours") == true -> protocolObj.getInt("timezone_offset_hours")
            root.has("timezone_offset_hours") -> root.getInt("timezone_offset_hours")
            root.has("tz_offset_hours") -> root.getInt("tz_offset_hours")
            else -> currentConfig.timezoneOffsetHours
        }

        // 3. App block
        val appObj = root.optJSONObject("app")
        val companyName = appObj?.optString("company_name")
            ?: root.optString("company_name", null)
        val hideSettingsUi = when {
            appObj?.has("hide_settings_ui") == true -> appObj.getBoolean("hide_settings_ui")
            root.has("hide_settings_ui") -> root.getBoolean("hide_settings_ui")
            else -> false
        }

        val companyCode = appObj?.optString("company_code")
            ?: root.optString("company_code", null)
        val employeeCode = appObj?.optString("employee_code")
            ?: root.optString("employee_code", null)

        val updatedSocketConfig = currentConfig.copy(
            wsUrl = wsUrl.ifBlank { "ws://$tcpHost:$tcpPort" },
            tcpHost = tcpHost.ifBlank { "avl.vtps.org" },
            tcpPort = if (tcpPort in 1..65535) tcpPort else 5200,
            useWebSocket = useWs,
            serverDigits = serverDigits.filter { it.isDigit() }.padStart(2, '0').takeLast(2).ifBlank { "01" },
            timezoneOffsetHours = tzOffset
        )

        return AppJsonConfig(
            socketConfig = updatedSocketConfig,
            companyName = companyName?.takeIf { it.isNotBlank() },
            hideSettingsUi = hideSettingsUi,
            companyCode = companyCode?.takeIf { it.isNotBlank() },
            employeeCode = employeeCode?.takeIf { it.isNotBlank() },
            serverDigits = serverDigits.filter { it.isDigit() }.padStart(2, '0').takeLast(2).ifBlank { "01" }
        )
    }

    /**
     * Reads JSON text from a content Uri (file picker).
     */
    fun readJsonFromUri(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader(Charsets.UTF_8).readText()
        } ?: throw IllegalArgumentException("Cannot open file from selected URI")
    }

    /**
     * Exports current config into a JSON string formatted for Farhan Bhai / demo use.
     */
    fun serializeConfigToJson(config: SocketConfig, hideSettings: Boolean, companyName: String? = null): String {
        val root = JSONObject()
        val server = JSONObject().apply {
            put("tcp_host", config.tcpHost)
            put("tcp_port", config.tcpPort)
            put("ws_url", config.wsUrl)
            put("use_websocket", config.useWebSocket)
        }
        val protocol = JSONObject().apply {
            put("server_digits", config.serverDigits)
            put("timezone_offset_hours", config.timezoneOffsetHours)
        }
        val app = JSONObject().apply {
            put("company_name", companyName ?: "Presence VTP")
            put("hide_settings_ui", hideSettings)
        }

        root.put("server", server)
        root.put("protocol", protocol)
        root.put("app", app)
        return root.toString(2)
    }

    /**
     * Silently checks if a `presence_config.json` exists in app internal storage
     * or standard external files dir (zero UI needed, pure backend/FOTA upload pattern).
     */
    fun findLocalConfigFile(context: Context): String? {
        val candidates = listOf(
            File(context.filesDir, "presence_config.json"),
            File(context.getExternalFilesDir(null), "presence_config.json")
        )
        for (f in candidates) {
            if (f.exists() && f.canRead()) {
                return try {
                    f.readText(Charsets.UTF_8)
                } catch (_: Exception) {
                    null
                }
            }
        }
        return null
    }
}
