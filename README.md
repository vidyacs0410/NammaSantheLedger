# Namma Santhe Ledger 📒

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.04-green.svg)](https://developer.android.com/jetpack/compose)
[![Hilt](https://img.shields.io/badge/Hilt-DI-orange.svg)](https://dagger.dev/hilt/)


**Namma Santhe Ledger** (ನಮ್ಮ ಸಂತೆ ಲೆಡ್ಜರ್) is a professional, secure, and highly accessible bookkeeping application designed specifically for small business owners, market vendors, and retailers in India. It simplifies the tracking of "Udari" (credit) and payments with a focus on regional language support and voice-first interaction.

---

## ✨ Key Features

### 🌍 Real-time Multilingual Support
Fully localized experience in **Kannada**, **Telugu**, **Tamil**, **Hindi**, and **English**. Switch languages instantly without restarting the app.

### 🎙️ Voice-First Management
- **Smart Voice Commands**: Record transactions by speaking. Say *"Add 500 for Rice"* or *"ಅಕ್ಕಿಗೆ 500 ಸೇರಿಸಿ"* and the app handles the rest.
- **Regional Digit Normalization**: Automatically converts regional numerals (೦-೯, ०-९) into standard digits for phone numbers and amounts.

### 🛡️ Enterprise-Grade Security
- **Biometric Lock**: Protect your financial data with Fingerprint or Face Unlock.
- **Secure Authentication**: Robust signup/login workflow with phone number verification and password strength enforcement.

### 📱 Modern & Intuitive UI
- Built entirely with **Jetpack Compose**.
- Beautiful "Market stall" themed design system.
- Seamless **Dark Mode** support.
- Responsive layouts for all screen sizes.

### 💾 Reliability & Backup
- **Offline-First**: Works perfectly without an internet connection.
- **Cloud & Local Backup**: Never lose your data; backup to the cloud or export local files.

---

## 🚀 Tech Stack

- **UI Framework**: Jetpack Compose (100%)
- **Architecture**: MVVM with Clean Architecture principles
- **Dependency Injection**: Dagger Hilt
- **Database**: Room Persistence Library
- **Navigation**: Compose Navigation
- **Image Loading**: Coil
- **Security**: Android Biometric Library
- **Background Tasks**: WorkManager
- **Speech**: Android SpeechRecognizer API

---

## 🛠️ Getting Started

### Prerequisites
- Android Studio Iguana or newer
- JDK 17
- Android SDK 26+ (Oreo)

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/namma-santhe-ledger.git
   ```
2. Open the project in Android Studio.
3. Sync Project with Gradle Files.
4. Run the app on your device or emulator.

---

## 🎤 Voice Command Examples

The app understands natural language in multiple regional scripts:

| Language | Action | Example Command |
| :--- | :--- | :--- |
| **English** | Add Udari | "Add 500 for Rice" |
| **Kannada** | ಸೇರಿಸಿ (Add) | "ಅಕ್ಕಿಗೆ 500 ಸೇರಿಸಿ" |
| **Hindi** | जोड़ो (Add) | "200 जोड़ो" |
| **Telugu** | జోడించు (Add) | "500 జోడించు" |

---
## 📸 Screenshots

| Splash | Home | Add Customer |
|---|---|---|
| ![](screenshots/splash_screen.png) | ![](screenshots/home_screen.png) | ![](screenshots/add_customer_screen.png) |

| Daily Summary | Profile | Settings |
|---|---|---|
| ![](screenshots/daily_summary_screen.png) | ![](screenshots/profile_screen.png) | ![](screenshots/settings_screen.png) |

| Help & Support | Security |
|---|---|
| ![](screenshots/help_support_screen.png) | ![](screenshots/security_screen.png) |

---

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 👩‍💻 Author

**Vidya CS**

---

## 📞 Support

If you encounter any issues or have questions, feel free to open an issue or contact the maintainers at [vidya.cs0410@gmail.com](mailto:vidya.cs0410@gmail.com).

**Made with ❤️ in India**

---

## 🌟 Future Enhancements

- AI-powered expense insights
- Cloud synchronization
- GST report generation
- WhatsApp bill sharing
- Multi-device sync

---