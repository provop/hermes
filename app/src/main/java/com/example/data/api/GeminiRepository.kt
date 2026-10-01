package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class GeminiResponse {
    data class Text(val content: String) : GeminiResponse()
    data class ToolCall(val functionName: String, val arguments: Map<String, Any?>, val speechResponse: String) : GeminiResponse()
    data class Error(val errorMessage: String) : GeminiResponse()
}

class GeminiRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val systemPrompt = """
        You are Hermes AI, a personal real-time voice AI assistant and intelligent interpreter for the user's Hermes Agent (connected via Telegram).
        You have direct control over Android device functions via function calling.
        You can make phone calls, draft emails, send SMS, browse the web, open Android settings (Wi-Fi, Bluetooth, Display, Sound, Battery, App Permissions, Assistant), toggle flashlight, set alarms, set timers, change ringer mode, or relay instructions to the user's Hermes Agent on Telegram.
        
        Keep your spoken answers concise, direct, helpful, and natural for voice conversation.
        When the user asks you to perform an action on the phone or relay a task to Hermes, ALWAYS call the appropriate function tool.
    """.trimIndent()

    private fun getToolDeclarations(): JSONArray {
        val toolsArray = JSONArray()

        // Function 1: make_call
        toolsArray.put(JSONObject().apply {
            put("name", "make_call")
            put("description", "Place a phone call or open phone dialer for a given phone number.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("phone_number", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The phone number or contact number to call")
                    })
                })
                put("required", JSONArray().apply { put("phone_number") })
            })
        })

        // Function 2: draft_email
        toolsArray.put(JSONObject().apply {
            put("name", "draft_email")
            put("description", "Draft an email to a recipient with subject and body.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("recipient", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The email address of the recipient")
                    })
                    put("subject", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The email subject line")
                    })
                    put("body", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The email body text")
                    })
                })
                put("required", JSONArray().apply {
                    put("recipient")
                    put("subject")
                    put("body")
                })
            })
        })

        // Function 3: send_sms
        toolsArray.put(JSONObject().apply {
            put("name", "send_sms")
            put("description", "Send or draft an SMS text message to a phone number.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("phone_number", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The target phone number")
                    })
                    put("message", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The text message content")
                    })
                })
                put("required", JSONArray().apply {
                    put("phone_number")
                    put("message")
                })
            })
        })

        // Function 4: browse_web
        toolsArray.put(JSONObject().apply {
            put("name", "browse_web")
            put("description", "Browse the web or search Google for a query or open a URL.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("query_or_url", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Search query or complete web URL")
                    })
                })
                put("required", JSONArray().apply { put("query_or_url") })
            })
        })

        // Function 5: open_settings
        toolsArray.put(JSONObject().apply {
            put("name", "open_settings")
            put("description", "Navigate to specific Android phone settings or app permissions.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("setting_type", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Target setting: wifi, bluetooth, display, sound, battery, accessibility, assistant, or permissions")
                    })
                })
                put("required", JSONArray().apply { put("setting_type") })
            })
        })

        // Function 6: toggle_flashlight
        toolsArray.put(JSONObject().apply {
            put("name", "toggle_flashlight")
            put("description", "Turn the device flashlight torch on or off.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("turn_on", JSONObject().apply {
                        put("type", "BOOLEAN")
                        put("description", "True to turn on, false to turn off")
                    })
                })
                put("required", JSONArray().apply { put("turn_on") })
            })
        })

        // Function 7: set_alarm
        toolsArray.put(JSONObject().apply {
            put("name", "set_alarm")
            put("description", "Set a device clock alarm.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("hour", JSONObject().apply {
                        put("type", "INTEGER")
                        put("description", "Hour of day (0-23)")
                    })
                    put("minutes", JSONObject().apply {
                        put("type", "INTEGER")
                        put("description", "Minutes (0-59)")
                    })
                    put("label", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Alarm label or title")
                    })
                })
                put("required", JSONArray().apply {
                    put("hour")
                    put("minutes")
                })
            })
        })

        // Function 8: set_timer
        toolsArray.put(JSONObject().apply {
            put("name", "set_timer")
            put("description", "Set a countdown timer on the device.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("seconds", JSONObject().apply {
                        put("type", "INTEGER")
                        put("description", "Duration in seconds")
                    })
                    put("label", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "Timer label or title")
                    })
                })
                put("required", JSONArray().apply { put("seconds") })
            })
        })

        // Function 9: set_ringer_mode
        toolsArray.put(JSONObject().apply {
            put("name", "set_ringer_mode")
            put("description", "Set the device audio ringer mode (normal, silent, or vibrate).")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("mode", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "normal, silent, or vibrate")
                    })
                })
                put("required", JSONArray().apply { put("mode") })
            })
        })

        // Function 10: relay_to_hermes
        toolsArray.put(JSONObject().apply {
            put("name", "relay_to_hermes")
            put("description", "Relay instructions, questions, or tasks directly to the user's Hermes Agent via Telegram.")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("message", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The message or task command to dispatch to the Hermes Agent")
                    })
                })
                put("required", JSONArray().apply { put("message") })
            })
        })

        return toolsArray
    }

    fun resolveValidModelName(requested: String?): String {
        val req = requested?.trim() ?: ""
        return when {
            req.contains("3.1", ignoreCase = true) || req.contains("pro", ignoreCase = true) -> "gemini-3.1-pro-preview"
            req.contains("lite", ignoreCase = true) -> "gemini-3.1-flash-lite-preview"
            req.contains("thinking", ignoreCase = true) -> "gemini-3.1-pro-preview"
            req.contains("live", ignoreCase = true) || req.contains("native-audio", ignoreCase = true) -> "gemini-3.5-flash"
            req == "gemini-3.5-flash" || req == "gemini-3.1-pro-preview" || req == "gemini-3.1-flash-lite-preview" -> req
            req.isNotBlank() -> req
            else -> "gemini-3.5-flash"
        }
    }

    suspend fun testApiKey(
        candidateKey: String,
        modelName: String = "gemini-3.5-flash"
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val cleanKey = candidateKey.trim()
        if (cleanKey.isBlank()) {
            return@withContext Pair(false, "API key cannot be empty")
        }

        val targetModel = resolveValidModelName(modelName)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$cleanKey"
        try {
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", "ping") })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("maxOutputTokens", 5)
                })
            }

            val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(url).post(requestBody).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Pair(true, "API key valid with model '$targetModel'!")
            } else if (targetModel != "gemini-3.5-flash") {
                // Fallback test with gemini-3.5-flash
                val fallbackUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$cleanKey"
                val fbReq = Request.Builder().url(fallbackUrl).post(requestBody).build()
                val fbResp = client.newCall(fbReq).execute()
                if (fbResp.isSuccessful) {
                    Pair(true, "API key valid with standard model 'gemini-3.5-flash'!")
                } else {
                    val fbBody = fbResp.body?.string() ?: ""
                    val errorMsg = try {
                        val root = JSONObject(fbBody)
                        root.optJSONObject("error")?.optString("message") ?: "HTTP ${fbResp.code}"
                    } catch (_: Exception) {
                        "HTTP ${fbResp.code}: $fbBody"
                    }
                    Pair(false, errorMsg)
                }
            } else {
                val errorMsg = try {
                    val root = JSONObject(responseBody)
                    root.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                } catch (_: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                Pair(false, errorMsg)
            }
        } catch (e: Exception) {
            Pair(false, "Network error: ${e.message}")
        }
    }

    suspend fun generateAssistantResponse(
        prompt: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        base64Image: String? = null,
        apiKeyOverride: String? = null,
        modelOverride: String? = null,
        enableExtendedThinking: Boolean = true
    ): GeminiResponse = withContext(Dispatchers.IO) {
        val effectiveApiKey = if (!apiKeyOverride.isNullOrBlank()) {
            apiKeyOverride.trim()
        } else {
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
        }

        if (effectiveApiKey.isBlank()) {
            return@withContext GeminiResponse.Error("Gemini API key is not configured. Please add your own API key in Settings (BYOK) or via the Secrets panel.")
        }

        val targetModel = resolveValidModelName(modelOverride)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$effectiveApiKey"

        try {
            val contentsArray = JSONArray()

            // Include past turns for context (up to last 6)
            val recentHistory = conversationHistory.takeLast(6)
            for ((role, text) in recentHistory) {
                val turnObj = JSONObject()
                turnObj.put("role", if (role.equals("USER", ignoreCase = true)) "user" else "model")
                val parts = JSONArray()
                parts.put(JSONObject().apply { put("text", text) })
                turnObj.put("parts", parts)
                contentsArray.put(turnObj)
            }

            // Current user turn
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()

            if (!base64Image.isNullOrBlank()) {
                val inlineData = JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", base64Image)
                }
                currentParts.put(JSONObject().apply { put("inlineData", inlineData) })
            }
            currentParts.put(JSONObject().apply { put("text", prompt) })
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            fun buildRequestJson(withThinking: Boolean): JSONObject = JSONObject().apply {
                put("contents", contentsArray)

                // System Instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })

                // Tools
                put("tools", JSONArray().apply {
                    put(JSONObject().apply {
                        put("functionDeclarations", getToolDeclarations())
                    })
                })

                // Generation Config with Extended Thinking
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                    if (withThinking && enableExtendedThinking && targetModel.contains("pro")) {
                        put("thinkingConfig", JSONObject().apply {
                            put("thinkingLevel", "high")
                        })
                    }
                })
            }

            var requestBody = buildRequestJson(withThinking = true).toString().toRequestBody(jsonMediaType)
            var request = Request.Builder().url(url).post(requestBody).build()
            var response = client.newCall(request).execute()
            var responseBody = response.body?.string() ?: ""

            // Fallback retry 1: If model doesn't support thinkingConfig
            if (!response.isSuccessful && responseBody.contains("thinkingConfig", ignoreCase = true)) {
                requestBody = buildRequestJson(withThinking = false).toString().toRequestBody(jsonMediaType)
                request = Request.Builder().url(url).post(requestBody).build()
                response = client.newCall(request).execute()
                responseBody = response.body?.string() ?: ""
            }

            // Fallback retry 2: If requested model is not found (404) or requires WebSocket (400), automatically use gemini-3.5-flash
            if (!response.isSuccessful && (response.code == 404 || response.code == 400 || responseBody.contains("not found", ignoreCase = true) || responseBody.contains("WebSocket", ignoreCase = true))) {
                val fallbackUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$effectiveApiKey"
                val fbBody = buildRequestJson(withThinking = false).toString().toRequestBody(jsonMediaType)
                val fbReq = Request.Builder().url(fallbackUrl).post(fbBody).build()
                val fbResp = client.newCall(fbReq).execute()
                if (fbResp.isSuccessful) {
                    response = fbResp
                    responseBody = fbResp.body?.string() ?: ""
                }
            }

            if (!response.isSuccessful) {
                return@withContext GeminiResponse.Error("Gemini API error (${response.code}): $responseBody")
            }

            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext GeminiResponse.Error("No response candidates returned from Gemini.")
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            if (parts == null || parts.length() == 0) {
                return@withContext GeminiResponse.Error("Empty content received from Gemini.")
            }

            // Check for functionCall
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                if (part.has("functionCall")) {
                    val functionCall = part.getJSONObject("functionCall")
                    val fnName = functionCall.getString("name")
                    val argsObj = functionCall.optJSONObject("args") ?: JSONObject()
                    val argsMap = mutableMapOf<String, Any?>()
                    val keys = argsObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        argsMap[key] = argsObj.get(key)
                    }

                    val speechText = when (fnName) {
                        "make_call" -> "Initiating call to ${argsMap["phone_number"]}."
                        "draft_email" -> "Drafting email to ${argsMap["recipient"]}."
                        "send_sms" -> "Composing SMS message for ${argsMap["phone_number"]}."
                        "browse_web" -> "Searching the web for ${argsMap["query_or_url"]}."
                        "open_settings" -> "Opening ${argsMap["setting_type"]} settings."
                        "toggle_flashlight" -> {
                            val on = argsMap["turn_on"] == true
                            if (on) "Turning on flashlight." else "Turning off flashlight."
                        }
                        "set_alarm" -> "Setting alarm for ${argsMap["hour"]}:${argsMap["minutes"]}."
                        "set_timer" -> "Setting timer for ${argsMap["seconds"]} seconds."
                        "set_ringer_mode" -> "Setting ringer mode to ${argsMap["mode"]}."
                        "relay_to_hermes" -> "Relaying command to your Hermes Telegram agent."
                        else -> "Executing $fnName."
                    }

                    return@withContext GeminiResponse.ToolCall(
                        functionName = fnName,
                        arguments = argsMap,
                        speechResponse = speechText
                    )
                }
            }

            // Otherwise extract text
            val textBuilder = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                if (part.has("text")) {
                    textBuilder.append(part.getString("text"))
                }
            }

            val resultText = textBuilder.toString().trim()
            if (resultText.isNotEmpty()) {
                GeminiResponse.Text(resultText)
            } else {
                GeminiResponse.Error("Gemini generated an empty text response.")
            }

        } catch (e: Exception) {
            Log.e("GeminiRepository", "Generation error", e)
            GeminiResponse.Error("Network error: ${e.message}")
        }
    }
}
