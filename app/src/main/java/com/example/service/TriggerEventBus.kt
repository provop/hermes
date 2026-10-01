package com.example.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class TriggerSource {
    POWER_BUTTON_ASSIST,
    VOLUME_BUTTON_HOTKEY,
    QUICK_SETTINGS_TILE,
    FLOATING_TRIGGER
}

object TriggerEventBus {
    private val _triggerEvents = MutableSharedFlow<TriggerSource>(extraBufferCapacity = 10)
    val triggerEvents: SharedFlow<TriggerSource> = _triggerEvents.asSharedFlow()

    fun emitTrigger(source: TriggerSource) {
        _triggerEvents.tryEmit(source)
    }
}
