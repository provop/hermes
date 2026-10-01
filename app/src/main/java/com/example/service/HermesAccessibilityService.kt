package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.MainActivity

class HermesAccessibilityService : AccessibilityService() {

    private var lastVolumeDownTime: Long = 0
    private val doubleClickIntervalMs = 500L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used
    }

    override fun onInterrupt() {
        // Clean up
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return false

        if (event.action == KeyEvent.ACTION_DOWN) {
            val keyCode = event.keyCode
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                val now = System.currentTimeMillis()
                if (now - lastVolumeDownTime < doubleClickIntervalMs) {
                    // Double click detected!
                    lastVolumeDownTime = 0
                    triggerAssistant()
                    return true // Consume key event
                } else {
                    lastVolumeDownTime = now
                }
            }
        }
        return super.onKeyEvent(event)
    }

    private fun triggerAssistant() {
        // Haptic feedback
        vibratePhone()

        TriggerEventBus.emitTrigger(TriggerSource.VOLUME_BUTTON_HOTKEY)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_START_LISTENING, true)
        }
        startActivity(intent)
    }

    private fun vibratePhone() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (_: Exception) {}
    }
}
