# SIPOS Kebab SK

Point of Sale (POS) and inventory management mobile app for Kebab SK operations.

This project consists of:
- Android mobile application (Kotlin + Jetpack Compose)
- Integration with Laravel REST API (SIINV Kebab SK)

## Key Features

### Cashier / POS
- Fast checkout with auto calculations
- Support for Cash and QRIS payment methods
- Digital receipts (Bluetooth thermal printer & text sharing)

### Inventory & Stock
- Daily stock management (open/close session)
- Record incoming ingredients
- Automatic recipe validation
- Manage menus, variants, and pricing

### Operational & Reporting
- Daily operational expenses
- Comprehensive transaction history
- Daily sales and revenue summary reports

### Authentication & Profile
- Secure login with API token
- Profile and password management
- Forgot password via Email OTP

## Technologies
- Kotlin 2.0
- Jetpack Compose + Material 3
- ViewModel + StateFlow
- Retrofit 2.11 + OkHttp 4.12
- Android Bluetooth API (ESC/POS)
- Gradle 9.1

## Important Structure
- `app/src/main/java/com/sipos/kebabsk/feature` -> Feature modules (auth, checkout, menu, etc.)
- `app/src/main/java/com/sipos/kebabsk/data/network` -> Network module and API configuration
- `app/src/main/java/com/sipos/kebabsk/ui/theme` -> UI styling and Material 3 theme
- `build.gradle.kts` -> Dependencies and configuration
- `local.properties` -> Local environment variables (API URL)

## Local Installation

1. Clone repository
```bash
git clone https://github.com/athayabismaj/sipos-kebab-sk.git
cd sipos-kebab-sk
```

2. API Configuration
Create `local.properties` in the root folder and set your API URLs:
```properties
API_BASE_URL_DEBUG=http://your-local-ip:8000/api/
API_BASE_URL_RELEASE=https://your-domain.com/api/
```

3. Build & Run
```bash
./gradlew assembleDebug
./gradlew installDebug
```

## Important Notes

1. Make sure the `siinv-kebab-sk` backend is running and accessible from your network.
2. For printing receipts, pair your Bluetooth thermal printer first in Android settings before selecting it in the app's Profile menu.
3. Release builds require an HTTPS domain for `API_BASE_URL_RELEASE`.
