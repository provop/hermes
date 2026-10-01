package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.HermesApplication
import com.example.data.api.GeminiRepository
import com.example.data.api.GeminiResponse
import com.example.data.api.TelegramHermesClient
import com.example.data.api.TelegramResult
import com.example.data.local.HermesActionLogEntity
import com.example.data.local.HermesMessageEntity
import com.example.data.local.HermesSettingsEntity
import com.example.device.ActionResult
import com.example.device.DeviceActionController
import com.example.service.TriggerEventBus
import com.example.service.TriggerSource
import com.example.voice.VoiceSpeechManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

enum class AssistantStatus {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

enum class TelegramConnectionStatus {
    DISCONNECTED,
    CHECKING,
    CONNECTED,
    FAILED
}

class HermesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as HermesApplication).database
    private val dao = db.hermesDao()

    val geminiRepo = GeminiRepository()
    val telegramClient = TelegramHermesClient()
    val deviceController = DeviceActionController(application)
    val voiceManager = VoiceSpeechManager(application)

    val messages: StateFlow<List<HermesMessageEntity>> = dao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentActions: StateFlow<List<HermesActionLogEntity>> = dao.getRecentActions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<HermesSettingsEntity?> = dao.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _status = MutableStateFlow(AssistantStatus.IDLE)
    val status: StateFlow<AssistantStatus> = _status.asStateFlow()

    private val _statusMessage = MutableStateFlow("Ready for voice or text commands")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _telegramStatus = MutableStateFlow(TelegramConnectionStatus.DISCONNECTED)
    val telegramStatus: StateFlow<TelegramConnectionStatus> = _telegramStatus.asStateFlow()

    private val _telegramBotName = MutableStateFlow("")
    val telegramBotName: StateFlow<String> = _telegramBotName.asStateFlow()

    private val _lastExecutedAction = MutableStateFlow<String?>(null)
    val lastExecutedAction: StateFlow<String?> = _lastExecutedAction.asStateFlow()

    private var telegramPollingJob: Job? = null
    private var lastTelegramOffset: Long = 0

    init {
        // Wire voice manager callbacks
        voiceManager.onSpeechFinalResult = { recognizedText ->
            handleUserInput(recognizedText)
        }

        voiceManager.onSpeechError = { errorMsg ->
            _status.value = AssistantStatus.ERROR
            _statusMessage.value = errorMsg
            viewModelScope.launch {
                delay(3000)
                _status.value = AssistantStatus.IDLE
                _statusMessage.value = "Ready"
            }
        }

        // Listen for hardware triggers (Power button assist, volume hotkey, Quick tile)
        viewModelScope.launch {
            TriggerEventBus.triggerEvents.collect { source ->
                val sourceLabel = when (source) {
                    TriggerSource.POWER_BUTTON_ASSIST -> "Power button / Assist gesture"
                    TriggerSource.VOLUME_BUTTON_HOTKEY -> "Volume button hotkey"
                    TriggerSource.QUICK_SETTINGS_TILE -> "Quick Settings tile"
                    TriggerSource.FLOATING_TRIGGER -> "Quick trigger"
                }
                _statusMessage.value = "Triggered via $sourceLabel"
                startListening()
            }
        }

        // Initialize default settings if not exists
        viewModelScope.launch {
            val existing = dao.getSettingsOnce()
            if (existing == null) {
                dao.saveSettings(HermesSettingsEntity())
            } else {
                if (existing.telegramBotToken.isNotBlank()) {
                    testTelegramConnection(existing.telegramBotToken)
                }
            }
        }
    }

    fun startListening() {
        _status.value = AssistantStatus.LISTENING
        _statusMessage.value = "Listening to your surroundings..."
        voiceManager.startListening()
    }

    fun stopListening() {
        voiceManager.stopListening()
        if (_status.value == AssistantStatus.LISTENING) {
            _status.value = AssistantStatus.IDLE
            _statusMessage.value = "Listening paused"
        }
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
        if (_status.value == AssistantStatus.SPEAKING) {
            _status.value = AssistantStatus.IDLE
            _statusMessage.value = "Ready"
        }
    }

    fun handleUserInput(input: String, environmentBitmap: Bitmap? = null) {
        val trimmed = input.trim()
        if (trimmed.isBlank() && environmentBitmap == null) return

        viewModelScope.launch {
            // Save user message to database
            val userMsg = HermesMessageEntity(
                sender = "USER",
                content = trimmed.ifBlank { "[Analyzing visual surroundings]" }
            )
            dao.insertMessage(userMsg)

            _status.value = AssistantStatus.THINKING
            _statusMessage.value = "Processing command with Hermes & Gemini..."

            val currentSettings = dao.getSettingsOnce() ?: HermesSettingsEntity()
            val mode = currentSettings.mode

            // Convert image to base64 if present
            var base64Img: String? = null
            if (environmentBitmap != null) {
                val stream = ByteArrayOutputStream()
                environmentBitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
                base64Img = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
            }

            // If mode is strictly HERMES_RELAY and token is present, send directly to Telegram Hermes agent
            if (mode == "HERMES_RELAY" && currentSettings.telegramBotToken.isNotBlank() && currentSettings.telegramChatId.isNotBlank()) {
                relayToTelegramHermes(trimmed, currentSettings)
                return@launch
            }

            // Gather recent conversation history
            val history = messages.value.takeLast(6).map { it.sender to it.content }

            // Query Gemini with tool calling & device control
            when (val geminiResult = geminiRepo.generateAssistantResponse(trimmed, history, base64Img)) {
                is GeminiResponse.Text -> {
                    val responseText = geminiResult.content
                    dao.insertMessage(
                        HermesMessageEntity(
                            sender = "GEMINI",
                            content = responseText
                        )
                    )
                    speakOrNotify(responseText, currentSettings)
                }

                is GeminiResponse.ToolCall -> {
                    executeDeviceTool(
                        geminiResult.functionName,
                        geminiResult.arguments,
                        geminiResult.speechResponse,
                        currentSettings
                    )
                }

                is GeminiResponse.Error -> {
                    _status.value = AssistantStatus.ERROR
                    _statusMessage.value = geminiResult.errorMessage
                    dao.insertMessage(
                        HermesMessageEntity(
                            sender = "SYSTEM",
                            content = "Hermes error: ${geminiResult.errorMessage}"
                        )
                    )
                }
            }
        }
    }

    private suspend fun executeDeviceTool(
        functionName: String,
        arguments: Map<String, Any?>,
        speechText: String,
        settings: HermesSettingsEntity
    ) {
        var actionResult: ActionResult? = null
        var actionTitle = functionName

        when (functionName) {
            "make_call" -> {
                val phone = arguments["phone_number"]?.toString() ?: ""
                actionTitle = "Phone Call"
                actionResult = deviceController.makeCall(phone)
            }
            "draft_email" -> {
                val to = arguments["recipient"]?.toString() ?: ""
                val subject = arguments["subject"]?.toString() ?: ""
                val body = arguments["body"]?.toString() ?: ""
                actionTitle = "Draft Email"
                actionResult = deviceController.draftEmail(to, subject, body)
            }
            "send_sms" -> {
                val phone = arguments["phone_number"]?.toString() ?: ""
                val msg = arguments["message"]?.toString() ?: ""
                actionTitle = "Send SMS"
                actionResult = deviceController.sendSms(phone, msg)
            }
            "browse_web" -> {
                val query = arguments["query_or_url"]?.toString() ?: ""
                actionTitle = "Web Search"
                actionResult = deviceController.browseWeb(query)
            }
            "open_settings" -> {
                val settingType = arguments["setting_type"]?.toString() ?: ""
                actionTitle = "Device Settings"
                actionResult = deviceController.openSettings(settingType)
            }
            "toggle_flashlight" -> {
                val turnOn = arguments["turn_on"] == true
                actionTitle = "Flashlight"
                actionResult = deviceController.toggleFlashlight(turnOn)
            }
            "set_alarm" -> {
                val hour = (arguments["hour"] as? Number)?.toInt() ?: 7
                val min = (arguments["minutes"] as? Number)?.toInt() ?: 0
                val label = arguments["label"]?.toString() ?: "Hermes Alarm"
                actionTitle = "Clock Alarm"
                actionResult = deviceController.setAlarm(hour, min, label)
            }
            "set_timer" -> {
                val seconds = (arguments["seconds"] as? Number)?.toInt() ?: 60
                val label = arguments["label"]?.toString() ?: "Hermes Timer"
                actionTitle = "Timer"
                actionResult = deviceController.setTimer(seconds, label)
            }
            "set_ringer_mode" -> {
                val mode = arguments["mode"]?.toString() ?: "normal"
                actionTitle = "Ringer Mode"
                actionResult = deviceController.setRingerMode(mode)
            }
            "relay_to_hermes" -> {
                val msg = arguments["message"]?.toString() ?: ""
                actionTitle = "Relay to Telegram Hermes"
                relayToTelegramHermes(msg, settings)
                return
            }
            else -> {
                actionResult = ActionResult.Failure("Unknown device tool: $functionName")
            }
        }

        val isSuccess = actionResult is ActionResult.Success
        val description = when (actionResult) {
            is ActionResult.Success -> actionResult.message
            is ActionResult.Failure -> actionResult.reason
            null -> "Action executed"
        }

        dao.insertAction(
            HermesActionLogEntity(
                actionType = functionName,
                title = actionTitle,
                description = description,
                isSuccess = isSuccess
            )
        )

        val fullResponse = "$speechText ${if (isSuccess) "Done." else "Failed: $description"}"
        dao.insertMessage(
            HermesMessageEntity(
                sender = "GEMINI",
                content = fullResponse,
                actionType = functionName,
                actionPayload = arguments.toString()
            )
        )

        _lastExecutedAction.value = actionTitle
        speakOrNotify(fullResponse, settings)
    }

    private suspend fun relayToTelegramHermes(text: String, settings: HermesSettingsEntity) {
        if (settings.telegramBotToken.isBlank() || settings.telegramChatId.isBlank()) {
            val errorMsg = "Telegram Bot Token or Chat ID not configured. Go to the Hermes Bridge tab to configure it."
            dao.insertMessage(
                HermesMessageEntity(
                    sender = "SYSTEM",
                    content = errorMsg
                )
            )
            speakOrNotify(errorMsg, settings)
            return
        }

        _status.value = AssistantStatus.THINKING
        _statusMessage.value = "Relaying to Hermes Agent on Telegram..."

        val sendResult = telegramClient.sendMessage(settings.telegramBotToken, settings.telegramChatId, text)
        when (sendResult) {
            is TelegramResult.Success -> {
                dao.insertAction(
                    HermesActionLogEntity(
                        actionType = "HERMES_RELAY",
                        title = "Telegram Dispatch",
                        description = "Sent \"$text\" to Hermes Agent",
                        isSuccess = true
                    )
                )

                // Poll briefly for reply from Hermes agent
                delay(2000)
                val updatesResult = telegramClient.getUpdates(settings.telegramBotToken, lastTelegramOffset)
                if (updatesResult is TelegramResult.Success) {
                    val (msgs, newOffset) = updatesResult.data
                    lastTelegramOffset = newOffset
                    val latestReply = msgs.lastOrNull { it.text != text }
                    if (latestReply != null) {
                        val replyText = "[Hermes Agent]: ${latestReply.text}"
                        dao.insertMessage(
                            HermesMessageEntity(
                                sender = "HERMES_TELEGRAM",
                                content = replyText
                            )
                        )
                        speakOrNotify(latestReply.text, settings)
                    } else {
                        val confirmation = "Command dispatched to Hermes Agent on Telegram."
                        dao.insertMessage(
                            HermesMessageEntity(
                                sender = "HERMES_TELEGRAM",
                                content = confirmation
                            )
                        )
                        speakOrNotify(confirmation, settings)
                    }
                } else {
                    speakOrNotify("Message dispatched to your Hermes Telegram channel.", settings)
                }
            }
            is TelegramResult.Error -> {
                val err = "Failed to reach Telegram Hermes: ${sendResult.message}"
                dao.insertMessage(HermesMessageEntity(sender = "SYSTEM", content = err))
                _status.value = AssistantStatus.ERROR
                _statusMessage.value = err
            }
        }
    }

    private fun speakOrNotify(speechContent: String, settings: HermesSettingsEntity) {
        _statusMessage.value = speechContent
        if (settings.autoSpeakResponses) {
            _status.value = AssistantStatus.SPEAKING
            voiceManager.speak(speechContent, settings.speechPitch, settings.speechRate)
        } else {
            _status.value = AssistantStatus.IDLE
        }
    }

    fun testTelegramConnection(token: String) {
        viewModelScope.launch {
            _telegramStatus.value = TelegramConnectionStatus.CHECKING
            when (val res = telegramClient.getBotInfo(token)) {
                is TelegramResult.Success -> {
                    _telegramStatus.value = TelegramConnectionStatus.CONNECTED
                    _telegramBotName.value = res.data.username ?: res.data.firstName
                }
                is TelegramResult.Error -> {
                    _telegramStatus.value = TelegramConnectionStatus.FAILED
                    _telegramBotName.value = res.message
                }
            }
        }
    }

    fun updateSettings(
        botToken: String,
        chatId: String,
        mode: String,
        volumeTrigger: Boolean,
        autoSpeak: Boolean,
        pitch: Float,
        rate: Float
    ) {
        viewModelScope.launch {
            val updated = HermesSettingsEntity(
                id = 1,
                telegramBotToken = botToken,
                telegramChatId = chatId,
                mode = mode,
                volumeKeyTriggerEnabled = volumeTrigger,
                autoSpeakResponses = autoSpeak,
                speechPitch = pitch,
                speechRate = rate
            )
            dao.saveSettings(updated)
            if (botToken.isNotBlank()) {
                testTelegramConnection(botToken)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.clearMessages()
            dao.clearActions()
        }
    }

    override fun onCleared() {
        super.onCleared()
        telegramPollingJob?.cancel()
        voiceManager.destroy()
    }
}
