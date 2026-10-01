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

## 🎙️ Hardware Triggers & Quick Access

1. **Power Button / Corner Swipe**:
   * Registered with Android's system `ACTION_ASSIST` intent.
   * In Android Settings (**Apps > Default Apps > Digital Assistant App**), choose **Hermes AI**.
   * Long-pressing the physical Power Button or swiping diagonally from bottom screen corners instantly activates voice listening.
2. **Volume Button Hotkey**:
   * Uses `HermesAccessibilityService` to detect a double-tap of the physical **Volume Down** key with haptic vibration feedback.
3. **Quick Settings Drawer Tile**:
   * Pull down the notification shade and add the **"Hermes Listen"** tile for 1-tap listening.

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
