<div align="center">
  <img src="app/src/main/res/drawable-nodpi/kebab_sk_logo.png" alt="Kebab SK logo" width="96" />
  <h1>Kebab SK — SIPOS</h1>
  <p><strong>An Android cashier application integrated with SIINV inventory and operations.</strong></p>

  [![Android API 26+](https://img.shields.io/badge/Android-API_26%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)
  [![Kotlin 2.0.21](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
  [![AGP 9.0.1](https://img.shields.io/badge/AGP-9.0.1-3DDC84?style=flat-square&logo=androidstudio&logoColor=white)](https://developer.android.com/build/releases/gradle-plugin)

  <br><br>

  <a href="README.md"><img src="https://img.shields.io/badge/Language-English-1E40AF?style=for-the-badge" alt="English" /></a>
  <a href="README-id.md"><img src="https://img.shields.io/badge/Bahasa-Indonesia-E11D48?style=for-the-badge" alt="Bahasa Indonesia" /></a>
</div>

---

## About SIPOS

SIPOS is an Android Point of Sale application for Kebab SK cashiers. It connects cashier authentication, daily stock sessions, menu catalogs, cash transactions, receipts, transaction history, operational expenses, and Bluetooth printers to the SIINV REST API.

SIPOS is a client application and requires a compatible SIINV backend, a valid cashier account, and an active daily stock session. Prices, branch access, menu availability, stock mutations, and transaction authorization remain authoritative on the backend.

The primary flow is:

1. A user signs in with an account that has the cashier role.
2. The app retrieves the profile, daily session status, menu catalog, stock availability, and sales summary from the backend.
3. The cashier selects menu variants and reviews the order in the cart.
4. A cash payment is submitted to the backend, where prices, sessions, branches, and ingredient availability are validated.
5. A successful transaction produces a digital receipt that can be shared or printed through a Bluetooth thermal printer.
6. Transaction history, daily stock, session closing, and operational expenses remain synchronized with SIINV.

## Key Features

### Cashier and transactions

- Cashier dashboard with the daily session status, transaction count, items sold, and current-day revenue.
- Paginated, locally cached menu catalog with categories, variant images, prices, and stock-based availability.
- Cart quantity controls, item removal, and automatic total calculation.
- **Cash payments** with quick amount options, received amount validation, change calculation, and duplicate submission prevention.
- Transaction receipts that can be displayed, shared as text, and printed through a Bluetooth thermal printer.
- Pull-to-refresh support on the dashboard, menu catalog, transaction history, daily stock, and profile screens.

### History and transaction cancellation

- Date-based transaction history, summaries, revenue, paid status, and cancelled status.
- Transaction details and receipt reprinting.
- Same-day transaction cancellation subject to session, ownership, authorization, and backend validation.
- `restock` or `waste` cancellation reasons are sent to the backend. Their final inventory effect depends on the backend contract and implementation in use.

### Daily stock and operations

- View ingredient balances and statuses for a daily stock session opened by an administrator.
- Enter physical remaining quantities and close the daily session.
- Record cashier operational expenses with amount and category validation.
- Preserve branch context from the active session and backend account assignment.

### Accounts and devices

- Token login, email OTP password recovery, profile updates, password changes, and logout.
- Encrypted session storage backed by Android Keystore.
- Select and persist the cashier's Bluetooth printer.
- Session-scoped state isolation so data from a previous user does not remain after switching accounts.

## Architecture and Technology

The codebase is organized by feature with presentation, domain, and data layers. The UI follows MVVM with reactive state flows.

| Area | Technology |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose, Material 3 |
| State | ViewModel, StateFlow, Kotlin Coroutines |
| Dependency injection | Koin 3.5.6 |
| Navigation | Navigation Compose 2.8.3 |
| API | Retrofit 2.11, OkHttp 4.12, Gson |
| Images | Coil Compose 2.7 |
| Local cache | Room 2.8.4 |
| Printer | Android Bluetooth API, ESC/POS |
| Build | Android Gradle Plugin 9.0.1, Gradle 9.1.0 |
| Android | Minimum API 26, compile/target API 36 |
| Tests | JUnit, coroutine test, Compose UI test |

Main structure:

```text
app/src/main/java/com/sipos/kebabsk/
├── common/        # shared utilities, sessions, validation, and components
├── di/            # dependency injection configuration
├── feature/       # auth, menu, cart, checkout, stock, transactions, and profile
└── ui/theme/      # Compose colors, typography, and theme
```

## Local Requirements

- Android Studio with support for Android Gradle Plugin 9.0.1.
- Gradle JDK 17 or newer. Android Studio's embedded JDK is sufficient, so a separate Java installation is not required.
- Android SDK Platform 36 and SDK Build Tools 36.0.0.
- An Android 8.0 (API 26) or newer device or emulator.
- A reachable [SIINV Kebab SK](https://github.com/athayabismaj/siinv-kebab-sk) backend.

## Local Installation

1. Clone the repository and enter the project directory.

   ```bash
   git clone https://github.com/athayabismaj/sk-pos.git
   cd sk-pos
   ```

2. Open the project in Android Studio and wait for Gradle Sync to finish.

3. Create or complete `local.properties` in the project root. Android Studio normally adds `sdk.dir` automatically.

   ```properties
   API_BASE_URL_DEBUG=http://local-server-ip:8000/api/
   API_BASE_URL_RELEASE=https://your-domain.com/api/
   ```

   Both URLs must end with `/api/`. Do not commit `local.properties`; it contains machine-specific configuration and may expose internal service addresses.

4. Select the debug address for the target device.

   - Android emulator with the backend on the same computer: use `http://10.0.2.2:8000/api/`.
   - Physical device: use the backend computer's LAN IPv4 address, place both devices on the same network, and allow the server port through the firewall.
   - A local backend must listen on a network interface reachable by the device, not only on `127.0.0.1`.

5. Run the app from Android Studio or through the Gradle Wrapper.

   Windows PowerShell:

   ```powershell
   .\gradlew.bat assembleDebug
   .\gradlew.bat installDebug
   ```

   Linux/macOS:

   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

## Build and Testing

Windows PowerShell:

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
```

Linux/macOS:

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest
```

Before release, verify the application against the target SIINV backend, representative cashier accounts, transaction scenarios, and the intended Bluetooth printer hardware.

Release builds accept only an HTTPS `API_BASE_URL_RELEASE` and reject the default placeholder and local hosts explicitly denied by the Gradle configuration. Release keystore configuration and signing credentials must be supplied privately by the deploying party.

## Bluetooth Printer

1. Pair the thermal printer through Android Bluetooth settings.
2. Open SIPOS and navigate to **Profile → Bluetooth Printer**.
3. Grant Bluetooth permissions when prompted, then select the paired printer.
4. Test printing from a successful transaction receipt or transaction details in the history screen.

Use a physical Android device for printer testing. ESC/POS command support varies by vendor, so paper width, encoding, and output must be verified on the target hardware.

## Security

- Never commit `local.properties`, API tokens, passwords, OTP codes, private service URLs, keystores, or signing credentials.
- Session tokens are stored using Android Keystore-backed encryption and excluded from application backups.
- Release builds disable cleartext traffic, debug network logging, and debuggable mode while enabling minification and resource shrinking.
- Prices, stock availability, active sessions, branches, and transaction authorization must remain backend-validated; client data must not be treated as the sole source of truth.
- Report security issues privately to the project owner and never include credentials or operational data in issue reports.

## Integration Notes

- The application requires a SIINV backend, a valid cashier account, and an active session.
- The implemented payment flow is cash-based.
- Session opening and stock distribution are performed by administrators through SIINV; the cashier app focuses on monitoring and closing sessions.
- Inventory behavior following transaction cancellation is determined by the backend and must be tested against the paired SIINV version.
- Email OTP, Bluetooth printing, and end-to-end transaction testing require their corresponding services, hardware, and test data.

## Technical Documentation

- [API integration](docs/API_INTEGRATION.md)
- [Android contract testing](docs/ANDROID_CONTRACT_TESTING.md)
- [API contract fixtures](docs/API_CONTRACT_FIXTURES.md)
- [Cashier mobile UAT scenarios](docs/UAT_E2E_MOBILE_KASIR.md)
- [Deployment security checklist](docs/security-deployment-checklist.md)

## Contributing

Use focused commits, include tests for behavioral changes, and document API contract changes that affect SIINV compatibility. Pull requests are reviewed according to project priorities and compatibility requirements.

## License

This repository does not currently include a SIPOS-specific `LICENSE` file. Contact the project owner before reuse, modification, redistribution, or commercial use. Frameworks, libraries, and third-party dependencies remain subject to their respective licenses.

Copyright © 2026 Kebab SK. All rights reserved.
