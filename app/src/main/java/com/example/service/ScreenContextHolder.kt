package com.example.service

object ScreenContextHolder {
    @Volatile
    var latestScreenText: String? = null

    @Volatile
    var latestScreenshotBase64: String? = null

    @Volatile
    var lastCaptureTimestamp: Long = 0

    fun hasRecentScreenContext(): Boolean {
        return (System.currentTimeMillis() - lastCaptureTimestamp) < 60_000 &&
                (!latestScreenText.isNullOrBlank() || !latestScreenshotBase64.isNullOrBlank())
    }

    fun setScreenContext(text: String?, base64Image: String?) {
        latestScreenText = text
        latestScreenshotBase64 = base64Image
        lastCaptureTimestamp = System.currentTimeMillis()
    }

    fun clear() {
        latestScreenText = null
        latestScreenshotBase64 = null
        lastCaptureTimestamp = 0
    }
}
