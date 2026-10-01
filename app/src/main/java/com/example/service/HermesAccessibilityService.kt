package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Base64
import android.view.Display
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.HermesPopupActivity
import com.example.MainActivity
import java.io.ByteArrayOutputStream

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
        // Capture screen text & screenshot BEFORE launching the overlay so it reflects the underlying app
        captureCurrentScreen()

        // Haptic feedback
        vibratePhone()

        TriggerEventBus.emitTrigger(TriggerSource.VOLUME_BUTTON_HOTKEY)

        // Launch floating popup overlay instead of full app
        val intent = Intent(this, HermesPopupActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }

    private fun captureCurrentScreen() {
        try {
            val rootNode = rootInActiveWindow
            val textBuilder = StringBuilder()
            extractNodeText(rootNode, textBuilder, 0, 8)
            val screenText = textBuilder.toString().trim()

            ScreenContextHolder.setScreenContext(
                text = if (screenText.isNotBlank()) screenText else null,
                base64Image = null
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                takeScreenshot(
                    Display.DEFAULT_DISPLAY,
                    mainExecutor,
                    object : TakeScreenshotCallback {
                        override fun onSuccess(screenshotResult: ScreenshotResult) {
                            try {
                                val buffer = screenshotResult.hardwareBuffer
                                val colorSpace = screenshotResult.colorSpace
                                val hwBitmap = Bitmap.wrapHardwareBuffer(buffer, colorSpace)
                                if (hwBitmap != null) {
                                    val softBitmap = hwBitmap.copy(Bitmap.Config.ARGB_8888, false)
                                    val stream = ByteArrayOutputStream()
                                    softBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                                    val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                                    ScreenContextHolder.latestScreenshotBase64 = base64
                                    hwBitmap.recycle()
                                    softBitmap.recycle()
                                }
                                buffer.close()
                            } catch (_: Exception) {}
                        }

                        override fun onFailure(errorCode: Int) {}
                    }
                )
            }
        } catch (_: Exception) {}
    }

    private fun extractNodeText(node: AccessibilityNodeInfo?, builder: StringBuilder, depth: Int, maxDepth: Int) {
        if (node == null || depth > maxDepth) return
        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()
        if (!text.isNullOrBlank() && !builder.contains(text)) {
            builder.append(text).append("\n")
        }
        if (!desc.isNullOrBlank() && !builder.contains(desc)) {
            builder.append(desc).append("\n")
        }
        for (i in 0 until node.childCount) {
            extractNodeText(node.getChild(i), builder, depth + 1, maxDepth)
        }
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
