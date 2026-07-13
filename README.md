# Sipos Kebab SK - Point of Sale & Inventory 🥙

[![id](https://img.shields.io/badge/lang-id-red.svg)](README-id.md)
[![en](https://img.shields.io/badge/lang-en-blue.svg)](README.md)

A modern, robust Point of Sale (POS) and inventory management Android application tailored for Kebab businesses. Built with **Kotlin** and **Jetpack Compose**, and seamlessly integrated with a **Laravel REST API** (`siinv-kebab-sk`).

![Android](https://img.shields.io/badge/Android-26%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?logo=jetpackcompose&logoColor=white)
![Retrofit](https://img.shields.io/badge/Retrofit-2.11-48B983)
![API](https://img.shields.io/badge/API-Laravel-FF2D20?logo=laravel&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow)

## ✨ Features

- **🛒 Cashier / POS**: Fast checkout process with automatic calculations, supporting Cash and QRIS payment methods.
- **📦 Menu & Variant Management**: Manage products, variants, pricing, and stock availability in real-time.
- **🧾 Digital Receipts**: Print physical receipts via **Bluetooth Thermal Printers** (ESC/POS) or share digital receipts as text.
- **📊 Transaction History**: Comprehensive daily sales reports and revenue summaries.
- **🏪 Daily Stock Management**: Open/close stock sessions, log incoming ingredients, and automatic recipe validation.
- **💰 Operational Expenses**: Record and track daily operational expenses directly from the app.
- **👤 Profile & Authentication**: Secure JWT/Token-based login, profile management, and password updates.
- **🔐 Forgot Password**: Secure password reset flow using Email OTP.
- **🎨 Premium UI/UX**: Modern design system utilizing Material 3, custom kebab-themed assets, splash screens, and smooth micro-animations.

## 🏗️ Architecture

This project strictly follows **Clean Architecture** principles to ensure scalability, testability, and separation of concerns.

```text
app/
├── common/              # Utility & helper functions
├── data/
│   └── network/         # NetworkModule (Retrofit + OkHttp)
├── feature/             # Feature modules (Auth, Checkout, Menu, etc.)
│   ├── presentation/    # UI Layer: Jetpack Compose + ViewModels
│   ├── domain/          # Domain Layer: UseCases & Models
│   └── data/            # Data Layer: Repositories & API Services
└── ui/
    └── theme/           # Material 3 Theme, Typography, Colors
```

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| **Language** | Kotlin 2.0 |
| **UI Framework** | Jetpack Compose + Material 3 |
| **State Management** | ViewModel + StateFlow |
| **Networking** | Retrofit 2.11 + OkHttp 4.12 |
| **Serialization** | Gson |
| **Hardware Integration** | Android Bluetooth API (ESC/POS Thermal Printers) |
| **Build System** | Gradle 9.1 + Version Catalog (TOML) |
| **Backend Integration** | Laravel REST API (`siinv-kebab-sk`) |
| **Min SDK** | Android 8.0 (API 26) |
| **Target SDK** | Android 16 (API 36) |

## 📋 Prerequisites

Before you begin, ensure you have met the following requirements:
- **Android Studio** Ladybug (or newer).
- **JDK 11** (or newer).
- **Android SDK** API 36.
- The **Laravel REST API** (`siinv-kebab-sk`) must be running locally or deployed to a server.

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/username/sipos-kebab-sk.git
cd sipos-kebab-sk
```

### 2. API Configuration
You need to point the app to your backend API. Create or edit `local.properties` in the project root and add your API URLs:

```properties
API_BASE_URL_DEBUG=http://your-local-ip:8000/api/
API_BASE_URL_RELEASE=https://your-production-domain.com/api/
```
*Note: Release builds strictly require HTTPS domains.*

### 3. Build & Run
You can run the app directly via Android Studio or using Gradle commands:
```bash
# Build the debug APK
./gradlew assembleDebug

# Install to a connected device/emulator
./gradlew installDebug
```

## 🖨️ Bluetooth Printer Setup

The app supports printing receipts using standard Bluetooth Thermal Printers (ESC/POS protocol):
1. Pair your thermal printer in your Android device's **Bluetooth Settings**.
2. Open the app and navigate to **Profile** → **Bluetooth Printer**.
3. Select your paired printer from the list.
4. The configuration will be saved for future transactions.

## 🤝 Contributing

Contributions are welcome! Please follow these steps:
1. Fork the project.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
