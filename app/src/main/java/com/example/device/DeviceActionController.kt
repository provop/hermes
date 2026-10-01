package com.example.device

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.Settings
import android.util.Log

sealed class ActionResult {
    data class Success(val message: String, val details: String = "") : ActionResult()
    data class Failure(val reason: String) : ActionResult()
}

class DeviceActionController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    fun makeCall(phoneNumber: String): ActionResult {
        return try {
            val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult.Success("Opened phone dialer for $phoneNumber", "Calling $cleanNumber")
        } catch (e: Exception) {
            Log.e("DeviceAction", "Call error", e)
            ActionResult.Failure("Failed to initiate call: ${e.message}")
        }
    }

    fun draftEmail(recipient: String, subject: String, body: String): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${Uri.encode(recipient)}")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult.Success("Drafted email to $recipient", "Subject: '$subject'")
        } catch (e: Exception) {
            Log.e("DeviceAction", "Email draft error", e)
            ActionResult.Failure("Failed to draft email: ${e.message}")
        }
    }

    fun sendSms(phoneNumber: String, message: String): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$phoneNumber")
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult.Success("Opened SMS to $phoneNumber", "Message: \"$message\"")
        } catch (e: Exception) {
            Log.e("DeviceAction", "SMS error", e)
            ActionResult.Failure("Failed to open SMS: ${e.message}")
        }
    }

    fun browseWeb(queryOrUrl: String): ActionResult {
        return try {
            val intent = if (queryOrUrl.startsWith("http://") || queryOrUrl.startsWith("https://")) {
                Intent(Intent.ACTION_VIEW, Uri.parse(queryOrUrl))
            } else {
                Intent(Intent.ACTION_WEB_SEARCH).apply {
                    putExtra(SearchManager.QUERY, queryOrUrl)
                }
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            ActionResult.Success("Searching web for \"$queryOrUrl\"")
        } catch (e: Exception) {
            Log.e("DeviceAction", "Browser error", e)
            ActionResult.Failure("Failed to open browser: ${e.message}")
        }
    }

    fun openSettings(target: String): ActionResult {
        return try {
            val (action, name) = when (target.lowercase().trim()) {
                "wifi", "wi-fi", "internet" -> Pair(Settings.ACTION_WIFI_SETTINGS, "Wi-Fi Settings")
                "bluetooth" -> Pair(Settings.ACTION_BLUETOOTH_SETTINGS, "Bluetooth Settings")
                "display", "brightness", "screen" -> Pair(Settings.ACTION_DISPLAY_SETTINGS, "Display Settings")
                "sound", "volume", "audio" -> Pair(Settings.ACTION_SOUND_SETTINGS, "Sound & Vibration Settings")
                "battery", "power" -> Pair(Settings.ACTION_BATTERY_SAVER_SETTINGS, "Battery Settings")
                "accessibility" -> Pair(Settings.ACTION_ACCESSIBILITY_SETTINGS, "Accessibility Settings")
                "assistant", "voice" -> Pair(Settings.ACTION_VOICE_INPUT_SETTINGS, "Default Assistant Settings")
                "permissions", "app_details", "app" -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return ActionResult.Success("Opened app permissions settings for Hermes AI")
                }
                else -> Pair(Settings.ACTION_SETTINGS, "System Settings")
            }

            val intent = Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult.Success("Opened $name")
        } catch (e: Exception) {
            Log.e("DeviceAction", "Settings error", e)
            ActionResult.Failure("Failed to open settings: ${e.message}")
        }
    }

    fun toggleFlashlight(turnOn: Boolean): ActionResult {
        return try {
            if (cameraManager == null) return ActionResult.Failure("Camera service not available")
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return ActionResult.Failure("No camera with flash found")

            cameraManager.setTorchMode(cameraId, turnOn)
            ActionResult.Success(if (turnOn) "Flashlight turned ON" else "Flashlight turned OFF")
        } catch (e: Exception) {
            Log.e("DeviceAction", "Flashlight error", e)
            ActionResult.Failure("Failed to toggle flashlight: ${e.message}")
        }
    }

    fun setAlarm(hour: Int, minutes: Int, label: String): ActionResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            val timeFormatted = String.format("%02d:%02d", hour, minutes)
            ActionResult.Success("Set alarm for $timeFormatted", "Label: $label")
        } catch (e: Exception) {
            Log.e("DeviceAction", "Alarm error", e)
            ActionResult.Failure("Failed to set alarm: ${e.message}")
        }
    }

    fun setTimer(seconds: Int, label: String): ActionResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult.Success("Set timer for ${seconds}s", "Label: $label")
        } catch (e: Exception) {
            Log.e("DeviceAction", "Timer error", e)
            ActionResult.Failure("Failed to set timer: ${e.message}")
        }
    }

    fun setRingerMode(mode: String): ActionResult {
        return try {
            if (audioManager == null) return ActionResult.Failure("Audio manager unavailable")
            when (mode.lowercase().trim()) {
                "silent" -> audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                "vibrate" -> audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                else -> audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
            ActionResult.Success("Ringer mode set to ${mode.uppercase()}")
        } catch (e: Exception) {
            Log.e("DeviceAction", "Ringer mode error", e)
            ActionResult.Failure("Could not set ringer mode: ${e.message}")
        }
    }

    fun launchAppByName(appNameQuery: String): ActionResult {
        return try {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(0)
            val target = packages.firstOrNull { appInfo ->
                val label = pm.getApplicationLabel(appInfo).toString()
                label.contains(appNameQuery, ignoreCase = true)
            }
            if (target != null) {
                val launchIntent = pm.getLaunchIntentForPackage(target.packageName)
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(launchIntent)
                    return ActionResult.Success("Opened ${pm.getApplicationLabel(target)}")
                }
            }
            ActionResult.Failure("Could not find installed app matching \"$appNameQuery\"")
        } catch (e: Exception) {
            ActionResult.Failure("Failed to launch app: ${e.message}")
        }
    }
}
