package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hermes_messages")
data class HermesMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "USER", "GEMINI", "HERMES_TELEGRAM", "SYSTEM"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String? = null, // e.g. "MAKE_CALL", "DRAFT_EMAIL", "SETTINGS", "HERMES_RELAY"
    val actionPayload: String? = null,
    val isAudioResponse: Boolean = false
)

@Entity(tableName = "hermes_actions")
data class HermesActionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actionType: String,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true
)

@Entity(tableName = "hermes_settings")
data class HermesSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val telegramBotToken: String = "",
    val telegramChatId: String = "",
    val hermesAgentName: String = "Hermes Primary",
    val mode: String = "HYBRID", // "LOCAL_GEMINI", "HERMES_RELAY", "HYBRID"
    val volumeKeyTriggerEnabled: Boolean = true,
    val autoSpeakResponses: Boolean = true,
    val speechPitch: Float = 1.0f,
    val speechRate: Float = 1.05f,
    val customGeminiApiKey: String = ""
)
