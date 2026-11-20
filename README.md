# CASAN Monitor

CASAN Monitor is a native Android app for internal engineering and operations. Day 2 adds CASAN admin sign-in and gates the dashboard, alerts, history, and settings on an authenticated session. Day 3 adds an account-scoped Notification Center for messages delivered to the app.

## Build and run

1. Open this directory in Android Studio with AGP 9.3 support. The Gradle daemon toolchain requests JetBrains JDK 25. The app targets Java and Kotlin JVM 17. Install Android SDK Platform 37.1, Build-Tools 36.0.0, and Platform-Tools.
2. Start the CASAN backend locally on port 8080 using its local development configuration. Debug defaults to `http://127.0.0.1:8080/`. Connect the emulator or device and run `adb reverse tcp:8080 tcp:8080` so Android can reach the backend on your computer.
3. Sync Gradle and run the `app` configuration on an API 26 or newer emulator or physical device. For another environment, set `casanBackendUrl=https://<your-casan-environment>/` in your user Gradle properties. Use the service base URL above `api/`, with a trailing slash. To override only debug, set `casanDebugBackendUrl`. Release accepts HTTPS only and requires `casanBackendUrl` for sign-in.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Project structure

```text
app/src/main/
├── AndroidManifest.xml
├── java/id/casan/monitor/
│   ├── CasanMonitorApp.kt
│   ├── MainActivity.kt
│   ├── core/          # Design system, navigation, DI bindings
│   ├── data/          # Session, theme preferences, and notification storage
│   ├── domain/        # Notification models, filtering, and repository contract
│   ├── feature/       # Dashboard, alerts, history, detail, settings, and login
│   └── notification/  # Messaging service and channels
└── res/               # Vector icons, strings, launch theme
app/src/test/          # Auth contracts, notification mapping and filters
gradle/                # Version catalog
```

Gradle Kotlin DSL uses AGP 9.3.1 with built-in Kotlin pinned to 2.4.21. The catalog pins Compose BOM 2026.02.01 (including Material 3), AndroidX Activity Compose 1.13.0, Lifecycle 2.11.0, Navigation Compose 2.10.2, Hilt 2.60.1, KSP 2.3.14, Firebase BOM 35.2.0, Google Services 4.6.0, and JUnit 4.13.2. The target SDK is Android 17 (API 37).

## Day 2 backend contracts

| Route | Contract |
|---|---|
| `POST /api/admin-login` | Accepts `email` and `password`. On success, returns `data.token`, `data.refresh_token`, `data.expires_in` in seconds, and `data.admin` with `ID`, `Name`, and `Email`. |
| `POST /api/refresh-token` | Accepts `refresh_token`. Returns a rotated token pair without a profile. An invalid refresh returns 401 and the app clears the session. |
| `POST /api/logout` | Accepts `refresh_token` to revoke. The app clears local credentials before attempting revocation. |
| `POST /api/firebase/token` | Actual router path, despite the handler's Swagger annotation. Requires bearer authentication and accepts `token` plus `platform: android`. The handler uses the server's `user_id`. |

The Firebase token handler requires a platform user session. Admin sessions provide `admin_id`, so Android token synchronization remains unavailable for Monitor. Firebase auto-init remains disabled. The checked-in backend exposes no device-unregister route. The app clears local association state and displayed notifications on sign-out.

The backend sends Firebase notification title/body. Its queue sender adds metadata such as `type`, `allocation_id`, and `expires_at`. No checked-in sender supplies monitoring incident identifiers or severity. Foreground notification payloads use the Info channel. In the background, Firebase displays notification payloads and uses the launcher intent. The app has no incident route for these payloads.

## Physical-device checks

- Use a staging HTTPS environment and a CASAN admin test account. Enter the admin email and password, then tap **Sign in**. Confirm Dashboard opens and Settings shows the admin account. Repeat with a wrong password and airplane mode.
- Reopen the app after closing its process and confirm the session restores. Sign out and press Back to check the protected screens are inaccessible.
- On API 33 or newer, open **Settings → Notifications** after sign-in. Exercise Allow and Deny, confirm the system dialog is offered once, and change permission from Android settings. Exercise all three appearance modes and restart the app after each change.
- Configure the approved CASAN Firebase project with an ignored `app/google-services.json` for package `id.casan.monitor`. Once an approved admin device-registration contract is available, verify initial token retrieval, rotation, retry, account changes, and foreground/background delivery and taps with a physical device. Exercise background delivery with the process stopped by Android as well. Force-stop is a separate Android state.
- Fresh installs use Critical and High at HIGH and Info at DEFAULT. Android preserves importance for existing channel IDs across upgrades and exposes changes in system notification settings.

## Day 2 verification

Six JVM tests passed using the Kotlin compiler and JUnit directly, including admin payload decoding and request serialization, refresh rotation, expiry and malformed responses, and the existing incident-filter tests. The Android manifest parsed as XML. Gradle sync, debug build, lint, and Android runtime checks were blocked in this workspace by Gradle socket restrictions. No debug APK was generated. Live sign-in, Keystore persistence, navigation, permission handling, and FCM delivery still require Android-device verification.

## Day 3 Notification Center

Alerts, History, Dashboard counts, and notification detail share an account-scoped SQLite repository. The existing Firebase service validates title/body and records supported messages before checking Android notification permission. A stable FCM message ID prevents duplicate rows and preserves read status. Opening a detail marks it read, and the resulting local update refreshes the unread count and both lists. History is newest first. Logout hides the account's rows, which remain available on the same device when that account signs back in. The SQLite owner combines the backend base URL and admin ID.

The backend exposes no notification list, read-status, or incident-detail endpoint. Refresh re-reads local storage. Queue event types map to the Charging queue category. The current payload provides no source or severity, so the UI omits source and displays Info priority. Critical, High, and Medium filters have no matches for these payloads. Read status is device-local.

Firebase automatically displays current backend notification payloads in the background and may never invoke the app's message callback. Those notifications may be absent from history, and their launcher taps lack the local notification ID. A tap on a foreground notification created by Monitor can open its saved detail after authentication. Live delivery still depends on Firebase configuration and an approved device-registration contract for Monitor admin sessions.

### Device verification

- Verify a fresh install shows the empty Alerts and History states. Use the light and dark Alerts previews in Android Studio for populated layouts.
- With an approved test device token, send a supported notification payload while Monitor is open and signed in. Confirm Alerts and Dashboard update, opening detail marks the row read, and history persists after restart. Exercise search, severity and category filters, duplicate message IDs, disabled system notification permission, and sign-out followed by a different admin session.
- Check foreground notification taps, background system display, and both themes on an Android device. Full server history, cross-device read state, and live admin push depend on backend capabilities described above.
