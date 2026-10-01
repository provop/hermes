# Hermes AI Assistant & Telegram Agent Interpreter

A real-time voice AI assistant and mobile interpreter for Hermes Agents on Telegram, featuring complete Android device control, hardware-level hotkey triggers, and a Retrofit-powered Telegram Bot integration.

---

## 📥 Direct APK Download & Installation

The ready-to-install debug APK is included directly in this repository:

👉 **[Download Hermes AI APK (`apk/hermes-ai.apk`)](./apk/hermes-ai.apk)**

### Installation Steps on Android:
1. Download **`apk/hermes-ai.apk`** to your Android device (or transfer it from your computer).
2. Open the downloaded `.apk` file using your device's file manager or browser downloads.
3. If prompted, enable **"Allow from this source"** or **"Install unknown apps"** in Android Settings.
4. Tap **Install** to complete the installation and launch **Hermes AI**.
5. *(Optional)* You can also export a fresh APK or AAB at any time directly from the **Google AI Studio** top-right settings menu (**Export APK/AAB** or **Push to GitHub**).

---

## 🔑 Bring Your Own Key (BYOK) & AI Model Engine

Hermes AI supports full **BYOK** (Bring Your Own Key) and custom AI model selection directly within the app settings:
* Open the **Triggers & Settings** tab in the app.
* Enter your personal Gemini API key under **Bring Your Own Key (BYOK)**.
* **Model Engine Selection**:
  * **Gemini Live 3.8 Extended Thinking (`gemini-live-3.8-extended-thinking`)**: Activates deep multi-step chain-of-thought reasoning with `thinkingConfig` (`thinkingLevel: "high"`) for maximum intelligence, complex tool calling, and surrounding interpretation.
  * **Gemini Live Native Audio (`gemini-2.5-flash-native-audio-preview-12-2025`)**: Bidirectional low-latency speech & conversational audio.
  * **Gemini 3.1 Pro (`gemini-3.1-pro-preview`)**: Deep STEM reasoning & advanced multi-turn task planning.
  * **Gemini 3.5 Flash (`gemini-3.5-flash`)**: Fast general-purpose intelligence.
* **Extended Thinking Mode**: High reasoning budget toggle (`thinkingConfig`) to let Hermes think through complex device operations before answering.
* Tap **Test Model & Key** to validate connectivity and quota.
* Tap **Save Config** to persist it securely on your device.
* Tap **Clear BYOK & Revert to App Default Key** at any time to switch back.

---

## ⚡ Retrofit Telegram Bot Architecture

Hermes AI uses the **Square Retrofit 2** library paired with **Moshi** (`converter-moshi`) and **OkHttp 3** for all interactions with the Telegram Bot API:

* **Base URL**: `https://api.telegram.org/`
* **Network Stack**:
  * `TelegramApiService`: Interface with suspend functions for:
    * `GET bot{token}/getMe`: Validates bot credentials and fetches bot username.
    * `POST bot{token}/sendMessage`: Dispatches instructions and interpreted messages to the Hermes Telegram Agent.
    * `GET bot{token}/getUpdates`: Polls incoming replies, logs, and answers from the Hermes Agent.
  * `TelegramRetrofitClient`: Singleton configured with `MoshiConverterFactory`, `KotlinJsonAdapterFactory`, and `HttpLoggingInterceptor`.
  * `TelegramHermesClient`: High-level coroutine-safe client handling connection state, payload extraction, and error diagnostics.

### Telegram Agent Configuration in the App:
1. Open the **Hermes Hub** tab in the app.
2. Enter your **Telegram Bot Token** (from `@BotFather`).
3. Enter your **Telegram Chat ID** (your personal chat ID or group/channel ID).
4. Tap **Save Credentials & Connect** or **Ping** to verify the connection.
5. Choose your preferred interpreter mode:
   * **Hybrid AI**: Gemini executes local phone tasks and relays agent-specific queries to your Hermes bot.
   * **Hermes Direct**: All voice and text instructions are routed directly to your Telegram Hermes Agent.
   * **Local Only**: Autonomous on-device assistant without Telegram relay.

---

## 🎙️ Hardware Triggers, Screen Vision & Dynamic Circular Overlay

Instead of pulling the entire heavy app into the foreground, invoking any hardware trigger opens the **Dynamic Circular Overlay (`HermesPopupActivity`)** directly on top of your current screen:
* **7-State Adaptive Instrument**:
  1. `CIRCULAR_LISTENING`: Glowing multi-ring gyroscopic orb with real-time waveform decibel feedback and speech-to-text transcript.
  2. `WORKING`: Dual orbital rotating ring with *"Reasoning through screen & device plan with Gemini Live 3.8 Extended Thinking"*.
  3. `DONE`: Emerald checkmark badge with action execution details (calls, timers, alarms, torch, Telegram relay).
  4. `REPLY`: Clean card displaying Hermes's spoken answer with audio TTS playback.
  5. `TYPING`: Smooth pill keyboard search field with quick suggestion tags.
  6. `ERROR`: Amber warning shield with one-tap retry button.
  7. `MINIMIZED_CHIP`: Collapses into a floating capsule chip `[ ⚡ Hermes 3.8 👁️ ]` on the edge of the screen so your app remains 100% visible while keeping Hermes 1-tap away.

### 👁️ Trigger-Activated Screen Reading (Vision Context):
* **Automatic On-Trigger Screen Capture**: Whenever you trigger Hermes (Volume Down double-tap, Long-press power, or corner swipe), `HermesAccessibilityService` extracts the visible text hierarchy (`rootInActiveWindow`) and captures a screen snapshot (`takeScreenshot`).
* **Visual Context Understanding**: The screen context is automatically provided to Gemini Live 3.8 Extended Thinking. You can say:
  * *"What is on my screen right now?"*
  * *"Summarize this article"*
  * *"Translate this message"*
  * *"Explain this error and tell me what to tap next"*
* **Zero Intrusiveness**: Screen reading is **only** triggered when you deliberately press a hardware trigger, keeping battery usage and privacy strictly optimal.

### Supported Triggers:
1. **Power Button / Corner Swipe**:
   * Registered with Android's system `ACTION_ASSIST` intent.
   * In Android Settings (**Apps > Default Apps > Digital Assistant App**), choose **Hermes AI**.
   * Long-pressing the physical Power Button or swiping diagonally from bottom screen corners opens the **Dynamic Circular Overlay**.
2. **Volume Button Hotkey**:
   * Double-tapping the physical **Volume Down** key captures the screen context and opens the **Dynamic Circular Overlay** with haptic feedback.
3. **Quick Settings Drawer Tile**:
   * Pull down notification quick settings and tap **"Hermes Listen"** to open the floating circular overlay.

---

## 📱 Device Control & Daily Productivity Tasks

Hermes AI uses Gemini tool calling to seamlessly interact with your Android device:
* **Phone Calls**: Voice dial any contact or number (`make_call`).
* **Draft Emails**: Pre-fills recipient, subject, and body in your email app (`draft_email`).
* **SMS Messages**: Compose and send text messages (`send_sms`).
* **Web Search**: Query Google or launch web URLs (`browse_web`).
* **Phone Settings**: Direct navigation to Wi-Fi, Bluetooth, Display, Sound, Battery, and App Permissions.
* **Flashlight**: Toggle hardware torch with voice or UI switch (`toggle_flashlight`).
* **Clock Alarms & Timers**: Set alarms or countdown timers (`set_alarm`, `set_timer`).
* **Ringer Modes**: Switch between Normal, Silent, and Vibrate.
