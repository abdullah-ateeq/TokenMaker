# TokenMaker 🔒

[![Android SDK](https://img.shields.io/badge/SDK-37-blue.svg)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-orange.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**TokenMaker** is a modern, highly secure, and privacy-focused Android application designed for generating cryptographically strong passwords, API keys, secrets, and random tokens on the go. Built from the ground up using Jetpack Compose and Material 3, TokenMaker combines sleek aesthetics with uncompromising cryptographic security and user privacy.

---

## 🌟 Key Features

- **Cryptographically Secure Generation**: Powered by `java.security.SecureRandom` using rejection-sampled bounded integers to eliminate modulo bias.
- **Unbiased Shuffling**: Guarantees true randomness via unbiased Fisher-Yates shuffling across selected character sets.
- **Granular Customization**:
  - Adjustable length (up to maximum secure limits).
  - Character set toggles: Uppercase (`A-Z`), Lowercase (`a-z`), Numbers (`0-9`), and Symbols (`!@#$%^&*...`).
  - Option to exclude ambiguous characters (e.g., `O`, `0`, `l`, `I`, `1`) for maximum readability.
  - Option to guarantee at least one character from every enabled character set.
- **Privacy & Security First**:
  - Automatic clearing of sensitive tokens when the app is backgrounded or stopped (`ON_STOP`).
  - Zero telemetry or data retention—your generated keys never leave your device.
- **Seamless UX & Material 3 Design**:
  - Dynamic Light & Dark theme support.
  - One-tap copy to clipboard with haptic feedback and instant confirmation snackbars.
  - Persistent user preference storage using Jetpack DataStore.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & [Material 3](https://m3.material.io/)
- **Architecture**: MVVM (Model-View-ViewModel) with Kotlin Coroutines & `StateFlow`
- **Persistence**: Android Jetpack DataStore (Preferences)
- **Security**: Java Cryptography Architecture (`SecureRandom`)
- **Navigation**: Jetpack Navigation Compose
- **Testing**: JUnit 4, Kotlin Coroutines Test

---

## 📱 Project Structure

```text
com.example.tokenmaker
│
├── data/
│   ├── preferences/       # User preferences repository & theme mode DataStore
│
├── domain/
│   ├── generator/         # Cryptographic token generator & validation logic
│   └── model/             # Character sets, token configs, and validation results
│
├── ui/
│   ├── about/             # About & information screen
│   ├── home/              # Main token generator screen & ViewModel
│   ├── navigation/        # Nav graph & screen definitions
│   ├── settings/          # App settings & preference management
│   ├── splash/            # Splash screen
│   ├── theme/             # Material 3 colors, typography, and shapes
│   └── util/              # Haptic feedback helpers
│
└── MainActivity.kt        # Application entry point
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug / Koala (or newer)
- Android SDK 37 (Compile/Target SDK)
- JDK 11 or higher

### Cloning & Running
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/TokenMaker.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and run the `:app` module on your emulator or physical device.

---

## 🧪 Testing

TokenMaker includes unit tests verifying cryptographic bounds, character set validation, and secure generation algorithms.

To run tests via command line:
```bash
./gradlew test
```

---

## 🛡️ License

This project is open-source under the [MIT License](LICENSE).

---

*Built with security and simplicity in mind by [Abdullah Ateeq](https://github.com/abdullah-ateeq).*
