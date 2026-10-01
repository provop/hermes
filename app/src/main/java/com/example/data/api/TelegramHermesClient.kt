package com.example.data.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class TelegramResult<out T> {
    data class Success<out T>(val data: T) : TelegramResult<T>()
    data class Error(val message: String) : TelegramResult<Nothing>()
}

data class TelegramBotInfo(
    val id: Long,
    val isBot: Boolean,
    val firstName: String,
    val username: String?
)

data class TelegramMessage(
    val updateId: Long,
    val messageId: Long,
    val senderName: String,
    val text: String,
    val date: Long
)

class TelegramHermesClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getBotInfo(botToken: String): TelegramResult<TelegramBotInfo> = withContext(Dispatchers.IO) {
        val cleanToken = botToken.trim()
        if (cleanToken.isEmpty()) {
            return@withContext TelegramResult.Error("Bot token is empty")
        }
        val url = "https://api.telegram.org/bot$cleanToken/getMe"
        try {
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext TelegramResult.Error("HTTP ${response.code}: $bodyString")
            }

            val json = JSONObject(bodyString)
            if (json.optBoolean("ok", false)) {
                val result = json.getJSONObject("result")
                val botInfo = TelegramBotInfo(
                    id = result.getLong("id"),
                    isBot = result.getBoolean("is_bot"),
                    firstName = result.getString("first_name"),
                    username = result.optString("username", null)
                )
                TelegramResult.Success(botInfo)
            } else {
                TelegramResult.Error(json.optString("description", "Failed to connect to Telegram bot"))
            }
        } catch (e: Exception) {
            Log.e("TelegramHermes", "getBotInfo error", e)
            TelegramResult.Error("Network error: ${e.message}")
        }
    }

    suspend fun sendMessage(botToken: String, chatId: String, text: String): TelegramResult<String> = withContext(Dispatchers.IO) {
        val cleanToken = botToken.trim()
        val cleanChatId = chatId.trim()
        if (cleanToken.isEmpty() || cleanChatId.isEmpty()) {
            return@withContext TelegramResult.Error("Bot token or Chat ID is missing")
        }

        val url = "https://api.telegram.org/bot$cleanToken/sendMessage"
        val payload = JSONObject().apply {
            put("chat_id", cleanChatId)
            put("text", text)
        }

        try {
            val requestBody = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(url).post(requestBody).build()
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext TelegramResult.Error("HTTP ${response.code}: $bodyString")
            }

            val json = JSONObject(bodyString)
            if (json.optBoolean("ok", false)) {
                TelegramResult.Success("Message dispatched to Hermes Agent")
            } else {
                TelegramResult.Error(json.optString("description", "Telegram API returned an error"))
            }
        } catch (e: Exception) {
            Log.e("TelegramHermes", "sendMessage error", e)
            TelegramResult.Error("Network error: ${e.message}")
        }
    }

    suspend fun getUpdates(botToken: String, offset: Long = 0): TelegramResult<Pair<List<TelegramMessage>, Long>> = withContext(Dispatchers.IO) {
        val cleanToken = botToken.trim()
        if (cleanToken.isEmpty()) {
            return@withContext TelegramResult.Error("Bot token is empty")
        }

        val url = "https://api.telegram.org/bot$cleanToken/getUpdates?offset=$offset&limit=10&timeout=2"
        try {
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext TelegramResult.Error("HTTP ${response.code}: $bodyString")
            }

            val json = JSONObject(bodyString)
            if (json.optBoolean("ok", false)) {
                val resultsArray = json.optJSONArray("result") ?: JSONArray()
                val messages = mutableListOf<TelegramMessage>()
                var highestUpdateId = offset

                for (i in 0 until resultsArray.length()) {
                    val updateObj = resultsArray.getJSONObject(i)
                    val updateId = updateObj.getLong("update_id")
                    if (updateId >= highestUpdateId) {
                        highestUpdateId = updateId + 1
                    }

                    val msgObj = updateObj.optJSONObject("message")
                        ?: updateObj.optJSONObject("channel_post")
                    if (msgObj != null) {
                        val text = msgObj.optString("text", "")
                        if (text.isNotBlank()) {
                            val from = msgObj.optJSONObject("from")
                            val sender = from?.optString("first_name", "Hermes Agent") ?: "Hermes Agent"
                            messages.add(
                                TelegramMessage(
                                    updateId = updateId,
                                    messageId = msgObj.optLong("message_id", 0),
                                    senderName = sender,
                                    text = text,
                                    date = msgObj.optLong("date", System.currentTimeMillis() / 1000)
                                )
                            )
                        }
                    }
                }
                TelegramResult.Success(Pair(messages, highestUpdateId))
            } else {
                TelegramResult.Error(json.optString("description", "Failed to retrieve updates"))
            }
        } catch (e: Exception) {
            Log.e("TelegramHermes", "getUpdates error", e)
            TelegramResult.Error("Network error: ${e.message}")
        }
    }
}
