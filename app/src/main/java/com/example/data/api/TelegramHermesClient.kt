package com.example.data.api

import android.util.Log
import com.example.data.api.telegram.SendMessageRequest
import com.example.data.api.telegram.TelegramRetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    private val apiService = TelegramRetrofitClient.apiService

    suspend fun getBotInfo(botToken: String): TelegramResult<TelegramBotInfo> = withContext(Dispatchers.IO) {
        val cleanToken = botToken.trim()
        if (cleanToken.isEmpty()) {
            return@withContext TelegramResult.Error("Bot token is empty")
        }

        try {
            val response = apiService.getMe(cleanToken)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.ok && body.result != null) {
                    val user = body.result
                    val info = TelegramBotInfo(
                        id = user.id,
                        isBot = user.isBot,
                        firstName = user.firstName,
                        username = user.username
                    )
                    TelegramResult.Success(info)
                } else {
                    TelegramResult.Error(body?.description ?: "Telegram API returned ok=false")
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                TelegramResult.Error("HTTP ${response.code()}: $errorBody")
            }
        } catch (e: Exception) {
            Log.e("TelegramHermes", "Retrofit getMe error", e)
            TelegramResult.Error("Network error: ${e.message}")
        }
    }

    suspend fun sendMessage(botToken: String, chatId: String, text: String): TelegramResult<String> = withContext(Dispatchers.IO) {
        val cleanToken = botToken.trim()
        val cleanChatId = chatId.trim()
        if (cleanToken.isEmpty() || cleanChatId.isEmpty()) {
            return@withContext TelegramResult.Error("Bot token or Chat ID is missing")
        }

        try {
            val request = SendMessageRequest(
                chatId = cleanChatId,
                text = text
            )
            val response = apiService.sendMessage(cleanToken, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.ok) {
                    TelegramResult.Success("Message dispatched to Hermes Agent")
                } else {
                    TelegramResult.Error(body?.description ?: "Failed to deliver message via Telegram")
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                TelegramResult.Error("HTTP ${response.code()}: $errorBody")
            }
        } catch (e: Exception) {
            Log.e("TelegramHermes", "Retrofit sendMessage error", e)
            TelegramResult.Error("Network error: ${e.message}")
        }
    }

    suspend fun getUpdates(botToken: String, offset: Long = 0): TelegramResult<Pair<List<TelegramMessage>, Long>> = withContext(Dispatchers.IO) {
        val cleanToken = botToken.trim()
        if (cleanToken.isEmpty()) {
            return@withContext TelegramResult.Error("Bot token is empty")
        }

        try {
            val response = apiService.getUpdates(
                token = cleanToken,
                offset = if (offset > 0) offset else null,
                limit = 10,
                timeout = 2
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.ok && body.result != null) {
                    val messages = mutableListOf<TelegramMessage>()
                    var highestUpdateId = offset

                    for (update in body.result) {
                        if (update.updateId >= highestUpdateId) {
                            highestUpdateId = update.updateId + 1
                        }

                        val msg = update.message ?: update.channelPost
                        if (msg?.text != null && msg.text.isNotBlank()) {
                            val sender = msg.from?.firstName ?: "Hermes Agent"
                            messages.add(
                                TelegramMessage(
                                    updateId = update.updateId,
                                    messageId = msg.messageId,
                                    senderName = sender,
                                    text = msg.text,
                                    date = msg.date
                                )
                            )
                        }
                    }
                    TelegramResult.Success(Pair(messages, highestUpdateId))
                } else {
                    TelegramResult.Error(body?.description ?: "Failed to fetch updates")
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                TelegramResult.Error("HTTP ${response.code()}: $errorBody")
            }
        } catch (e: Exception) {
            Log.e("TelegramHermes", "Retrofit getUpdates error", e)
            TelegramResult.Error("Network error: ${e.message}")
        }
    }
}
