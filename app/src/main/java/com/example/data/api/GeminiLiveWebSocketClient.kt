package com.example.data.api

import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GeminiLiveWebSocketClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // Keep alive
        .build()

    private var webSocket: WebSocket? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _liveAudioTranscript = MutableStateFlow("")
    val liveAudioTranscript: StateFlow<String> = _liveAudioTranscript.asStateFlow()

    fun connect(apiKeyOverride: String? = null, onResponseText: ((String) -> Unit)? = null) {
        val key = apiKeyOverride?.trim()?.ifBlank { null }
            ?: (if (BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") BuildConfig.GEMINI_API_KEY else "")

        if (key.isBlank()) return

        val wsUrl = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=$key"
        val request = Request.Builder().url(wsUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _isConnected.value = true
                Log.d("GeminiLiveWS", "Connected to Gemini Live Bidirectional WebSocket")

                // Send setup frame for real-time conversation
                val setupJson = JSONObject().apply {
                    put("setup", JSONObject().apply {
                        put("model", "models/gemini-2.5-flash-native-audio-preview-12-2025")
                        put("generationConfig", JSONObject().apply {
                            put("responseModalities", JSONArray().apply {
                                put("AUDIO")
                                put("TEXT")
                            })
                            put("speechConfig", JSONObject().apply {
                                put("voiceConfig", JSONObject().apply {
                                    put("prebuiltVoiceConfig", JSONObject().apply {
                                        put("voiceName", "Aoede")
                                    })
                                })
                            })
                        })
                    })
                }
                webSocket.send(setupJson.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val root = JSONObject(text)
                    val serverContent = root.optJSONObject("serverContent")
                    val modelTurn = serverContent?.optJSONObject("modelTurn")
                    val parts = modelTurn?.optJSONArray("parts")
                    if (parts != null) {
                        for (i in 0 until parts.length()) {
                            val part = parts.getJSONObject(i)
                            val txt = part.optString("text", "")
                            if (txt.isNotBlank()) {
                                _liveAudioTranscript.value = txt
                                onResponseText?.invoke(txt)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GeminiLiveWS", "Error parsing live message: ${e.message}")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _isConnected.value = false
                Log.w("GeminiLiveWS", "WebSocket failure (falling back to REST): ${t.message}")
            }
        })
    }

    fun sendTextQuery(text: String) {
        val clientContent = JSONObject().apply {
            put("clientContent", JSONObject().apply {
                put("turns", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", text) })
                        })
                    })
                })
                put("turnComplete", true)
            })
        }
        webSocket?.send(clientContent.toString())
    }

    fun disconnect() {
        webSocket?.close(1000, "Normal closure")
        webSocket = null
        _isConnected.value = false
    }
}
