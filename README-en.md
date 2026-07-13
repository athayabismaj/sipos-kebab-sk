<div align="center">
  <img src="https://raw.githubusercontent.com/android/architecture-samples/main/app/src/main/ic_launcher-web.png" alt="Android App Logo" width="100" />
  <h1>Kebab SK - SIPOS</h1>
  <p><b>Cloud-Based Mobile Point of Sales & Inventory Application</b></p>
  
  [![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)
  [![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
  [![Retrofit](https://img.shields.io/badge/Retrofit-48B983?style=flat-square)](https://square.github.io/retrofit/)
  
  <br>
  
  <a href="README.md"><img src="https://img.shields.io/badge/-ID-E11D48?style=for-the-badge" alt="ID" /></a>
  &nbsp;&nbsp;
  <a href="README-en.md"><img src="https://img.shields.io/badge/-ENG-1E40AF?style=for-the-badge" alt="ENG" /></a>
</div>

---

## 📖 About the Project
**SIPOS (Point of Sales System)** is a mobile cashier and inventory management application seamlessly integrated with the SIINV Kebab SK backend. Built with modern Android architecture standards (Clean Architecture) using Kotlin and Jetpack Compose. SIPOS is designed to accelerate daily transactions, print digital/physical receipts, and validate stock availability in real-time.

---

## ✨ Key Features

### 🛒 Cashier / POS
- 🚀 **Fast Transactions:** Dynamic checkout processing with automated price calculations.
- 💳 **Payment Methods:** Multi-payment support including Cash and QRIS.
- 🧾 **Digital Receipts:** Print physical receipts via **Bluetooth Thermal Printers** (ESC/POS) or share directly as text.

### 📦 Inventory & Daily Stock
- 🏪 **Operational Sessions:** Manage daily store opening and closing sessions.
- 📋 **Input Incoming Stock:** Record raw materials (bread, meat, vegetables) received from the central branch.
- 🔄 **Automatic Validation:** Real-time checking of ingredient availability based on recipe requirements.

### 📊 Reporting & Operations
- 💰 **Store Expenses:** Record daily operational expenses directly from the application.
- 📈 **Transaction History:** Monitor daily revenue summaries and sales history.

### 🔐 Authentication & Security
- 🔑 **Secure Access:** Login using API Bearer Tokens.
- 🛡️ **Account Recovery:** Password management and Forgot Password flow with Email OTP verification.

---

## 🛠️ Architecture & Technologies
This project implements **Clean Architecture** (Presentation, Domain, Data) to keep the codebase structured and testable.
- **Language:** Kotlin 2.0
- **UI Framework:** Jetpack Compose + Material 3
- **State Management:** ViewModel + StateFlow
- **Networking:** Retrofit 2.11 + OkHttp 4.12 + Gson
- **Hardware Integration:** Android Bluetooth API (ESC/POS)
- **Minimum SDK:** API 26 (Android 8.0) | **Target SDK:** API 36 (Android 16)

---

## 🚀 Getting Started (Local Setup)

To run this Android project on your local machine, follow these steps:

1. **Clone the Repository**
   ```bash
   git clone https://github.com/athayabismaj/sipos-kebab-sk.git
   cd sipos-kebab-sk
   ```
2. **API URL Configuration**
   The application requires a connection to the SIINV backend. Create a `local.properties` file in the project's root folder and add the following configurations:
   ```properties
   API_BASE_URL_DEBUG=http://your-local-ip:8000/api/
   API_BASE_URL_RELEASE=https://your-production-domain.com/api/
   ```
   *(Replace `your-local-ip` with the IP of your running SIINV server).*
3. **Build & Run the App**
   Open this project using **Android Studio (Ladybug or newer)**.
   Alternatively, run the following Gradle commands in your terminal:
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

---

## 🖨️ Bluetooth Printer Guide

The application supports direct printing via thermal printers:
1. Open your Android device's **Bluetooth Settings** and pair your thermal printer.
2. Open the SIPOS app, navigate to the **Profile** menu -> **Bluetooth Printer**.
3. Select your printer from the available list. This configuration will be saved automatically.

---
<br />
<div align="center">
  <sub>All Rights Reserved. Built for the operational excellence of <b>Kebab SK</b> &copy; 2026.</sub>
</div>
