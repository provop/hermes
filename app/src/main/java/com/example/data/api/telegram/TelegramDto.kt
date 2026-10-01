package com.example.data.api.telegram

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TelegramResponse<T>(
    @Json(name = "ok") val ok: Boolean,
    @Json(name = "result") val result: T? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "error_code") val errorCode: Int? = null
)

@JsonClass(generateAdapter = true)
data class TelegramUserDto(
    @Json(name = "id") val id: Long,
    @Json(name = "is_bot") val isBot: Boolean,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String? = null,
    @Json(name = "username") val username: String? = null
)

@JsonClass(generateAdapter = true)
data class TelegramChatDto(
    @Json(name = "id") val id: Long,
    @Json(name = "type") val type: String,
    @Json(name = "title") val title: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "first_name") val firstName: String? = null
)

@JsonClass(generateAdapter = true)
data class TelegramMessageDto(
    @Json(name = "message_id") val messageId: Long,
    @Json(name = "from") val from: TelegramUserDto? = null,
    @Json(name = "chat") val chat: TelegramChatDto,
    @Json(name = "date") val date: Long,
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class TelegramUpdateDto(
    @Json(name = "update_id") val updateId: Long,
    @Json(name = "message") val message: TelegramMessageDto? = null,
    @Json(name = "channel_post") val channelPost: TelegramMessageDto? = null
)

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    @Json(name = "chat_id") val chatId: String,
    @Json(name = "text") val text: String,
    @Json(name = "parse_mode") val parseMode: String? = null
)
