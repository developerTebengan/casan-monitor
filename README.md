# CASAN Monitor

CASAN Monitor is a native Android app for internal engineering and operations. CASAN Monitor uses CASAN admin sign-in with email and password and gates the dashboard, alerts, history, and settings on an authenticated session. Day 3 adds an account-scoped Notification Center for messages delivered to the app.

See the [Day 5 test report](docs/DAY5_TEST_REPORT.md) for verification results, release signing, and the device test procedure.

## Build and run

1. Open this directory in Android Studio with AGP 9.3 support. The Gradle daemon toolchain requests JetBrains JDK 25. The app targets Java and Kotlin JVM 17. Install Android SDK Platform 37.1, Build-Tools 36.0.0, and Platform-Tools.
2. Start the CASAN backend locally on port 8080 using its local development configuration. Debug defaults to `http://127.0.0.1:8080/`. Connect the emulator or device and run `adb reverse tcp:8080 tcp:8080` so Android can reach the backend on your computer.
3. Sync Gradle and run the `app` configuration on an API 26 or newer emulator or physical device. For staging debug builds, set `casanDebugBackendUrl` to the approved staging HTTPS base URL in user Gradle properties. Use the service base URL above `api/`, with a trailing slash. Release uses `casanBackendUrl` and accepts HTTPS only. Debug defaults to loopback.

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

## Backend contracts

| Route | Contract |
|---|---|
| `POST /api/admin-login` | Accepts `email` and `password`. Returns `data.token`, `data.refresh_token`, `data.expires_in` in seconds, and `data.admin` with `ID`, `Name`, and `Email`. |
| `POST /api/refresh-token` | Accepts `refresh_token`. Returns a rotated token pair without a profile. An invalid refresh returns 401 and the app clears the session. |
| `POST /api/logout` | Accepts `refresh_token` to revoke. The app clears local credentials before attempting revocation. |
| `POST /api/firebase/admin-token` | Requires an admin bearer token and accepts `token` plus `platform: android`. The backend gets the admin ID from the session. Apply `backend/migrations/20261123_admin_firebase_tokens.sql` before testing registration. |

Firebase token sync starts after an admin session is restored or created. It also responds to connectivity changes and token rotation. In-process retry is bounded to three attempts per trigger, with exponential backoff between attempts; there is no polling. Settings shows Registered after a confirmed request or a match against the persisted account, environment, and token fingerprint from a confirmed request. When Firebase configuration is missing, Settings reports that explicitly.

Updating from the phone and OTP build requires a new sign-in. User notification history is isolated from admin history, even when numeric IDs match. The backend exposes no unregister route. Sign-out clears the local association, cancels displayed notifications, and attempts Firebase token deletion for this app installation plus backend refresh-token revocation. If token deletion fails or times out, Android-managed background notifications for the old token may still arrive until Firebase invalidates it.

The backend sends Firebase notification title/body. Its queue sender adds metadata such as `type`, `allocation_id`, and `expires_at`. No checked-in sender supplies monitoring incident identifiers or severity. Foreground notification payloads use the Info channel. In the background, Firebase displays notification payloads and uses the launcher intent. The app has no incident route for these payloads.

## Physical-device checks

1. Configure a reachable backend with a working CASAN admin test account. On localhost, run `adb reverse tcp:8080 tcp:8080` after connecting the test device.
2. Put the approved CASAN Firebase project's `google-services.json` at `app/google-services.json` for package `id.casan.monitor`. Rebuild the debug APK so the Google Services plugin applies.
3. Enter the admin email and password, then tap **Sign in**. Check Dashboard, the admin identity in Settings, invalid credential handling, and session restoration after restarting the app.
4. Open **Settings → Notifications**. Check that it shows **Device registered**, then grant Android notification permission if needed. Use an authorized admin sender in the deployed CASAN environment if available. The checked-in charging, rental, and queue senders target user devices. An authorized Firebase Console message can check Firebase connectivity independently. Test the app in the foreground, then in the background. In the foreground, confirm arrival in Alerts and verify that opening detail updates the unread count. In the background, verify the system tray independently from local history.
5. Use airplane mode and reconnect, then test sign-out and sign-in with another account. Verify that the previous account's history stays hidden and device registration runs for the new admin.

## Day 3 Notification Center

Alerts, History, Dashboard counts, and notification detail share an account-scoped SQLite repository. The existing Firebase service validates title/body and records supported messages before checking Android notification permission. A stable FCM message ID prevents duplicate rows and preserves read status. Opening a detail marks it read, and the resulting local update refreshes the unread count and both lists. History is newest first. Logout hides the account's rows, which remain available on the same device when that account signs back in. The SQLite owner combines the backend base URL, admin principal type, and backend admin ID.

The backend exposes no notification list, read-status, or incident-detail endpoint. Refresh re-reads local storage. Previously cached queue items retain the Charging queue category. The admin messaging callback ignores the known customer queue types. The current supported payload provides no source or severity, so the UI omits source and displays Info priority. Critical, High, and Medium filters have no matches for these payloads. Read status is device-local.

Firebase automatically displays current backend notification payloads in the background and may never invoke the app's message callback. Those notifications may be absent from history, and their launcher taps lack the local notification ID. A tap on a foreground notification created by Monitor can open its saved detail after authentication. Live delivery still depends on Firebase configuration and a registered CASAN admin token.

### Device verification

- Verify a fresh install shows the empty Alerts and History states. Use the light and dark Alerts previews in Android Studio for populated layouts.
- With an approved test device token, send a supported notification payload while Monitor is open and signed in. Confirm Alerts and Dashboard update, opening detail marks the row read, and history persists after restart. Exercise search, severity and category filters, duplicate message IDs, disabled system notification permission, and sign-out followed by a different admin session.
- Check foreground notification taps, background system display, and both themes on an Android device. Full server history and cross-device read state depend on backend capabilities described above.

## Day 4 notification reliability

- Alerts and History support pull-to-refresh, and Dashboard has a refresh action. History groups by local date and indicates when Android reports no validated internet connection. A foreground transition and connectivity restoration re-read SQLite. A connectivity restoration can retry a transient session refresh once; there is no polling.
- The newest 250 saved notifications are loaded into the lists. Dashboard counts, search, and filters use this window. Older rows are retained for deduplication and remain available through valid detail taps. SQLite lookups remain scoped to the current backend environment and admin account. Signing out removes access to the account's cache and clears its navigation state. Offline access requires a valid restored session; an expired session needs the backend before protected screens become available again.
- App-generated taps use an immutable pending intent with the owning account and local notification ID. The pending target survives authentication and Activity recreation, and opening the same detail again does not stack another route. Invalid targets and taps for another account open Alerts without showing the requested record. Android-managed queue notification taps open Alerts when Firebase supplies the existing queue event type. Notification-only background messages can still open the app's normal entry point without a matching detail.
- Stable Firebase message IDs deduplicate across restarts. When no ID is provided, each callback receives a separate local ID so distinct notifications with identical text are preserved. ID-less redeliveries can therefore appear more than once. The app leaves background system notification rendering to Firebase. Existing channel settings are preserved; newly created High and Critical channels enable vibration and use Android's default sound behavior. Android and the user control final channel settings and Do Not Disturb.
- The backend has no history, read-state, incident detail, or device-unregister route. Device registration uses the admin token route. A token callback triggers synchronization while the app process has an active session; a cold start rechecks the current Firebase token after session restoration.

### Day 4 device checks

1. Install a fresh debug APK from Android Studio. Open Alerts and History in both themes, try larger fonts, and check refresh and empty states.
2. With an approved test token, send a supported payload in foreground, background, and after Android reclaims the process. Test the tap from signed-in and signed-out states, repeat a tap, deny notification permission, and disable the Info channel. Confirm that app-delivered messages still reach local history when system display is disabled.
3. With a valid session, open populated History in airplane mode, mark an item read, restart, and reconnect. Verify that sign-out and a different admin login hide the previous account's cache. Use an expired session to verify that access resumes only after backend refresh or login.
