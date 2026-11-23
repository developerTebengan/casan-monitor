# CASAN Monitor Day 5 test report

**Release decision: RELEASE BLOCKED.** An approved signed APK, device installation, authenticated registration, and real CASAN-to-device delivery have no execution evidence. The checked-in backend registers admin tokens but provides no sender that reads them. Confirm whether the deployed environment already has an authorized admin-alert sender.

## Audit

- Android uses Compose Material 3, Hilt, Retrofit and OkHttp, and DataStore for the theme. Source defaults are `id.casan.monitor`, CASAN Monitor, version `0.1.0` / code `1`, API 26 minimum, API 37 target, and brand color `#2DBA9D`. The manifest references the existing app icon, notification service and default Info channel.
- Admin login, refresh, logout, and authenticated registration use the existing backend routes. The register request includes the Android platform and an explicit bearer header. Session credentials are stored in an Android Keystore-backed AES-GCM file excluded from backup. The reviewed HTTP client has a 15-second call timeout and no logging interceptor. The Firebase token is sent to the backend and compared locally by fingerprint.
- An ignored `app/google-services.json` contains a matching Android client for the package. Deployed Firebase project ownership, backend alignment, and on-device initialization were unverified. The admin token table requires the existing backend migration `20261123_admin_firebase_tokens.sql`.
- The Notification Center uses account- and environment-scoped SQLite. The newest 250 records feed list, filter, and unread calculations. Read state is local, and refresh re-queries storage. Existing rows are retained for same-account offline use and deduplication. The runtime navigation uses the local notification repository. Mock incidents are referenced by an unused legacy ViewModel and preview code.
- The backend has no checked-in notification history, read-state, incident detail, or device unregistration route. The inspected customer senders emit notification payloads and sometimes customer queue metadata. They do not supply a monitoring severity or admin detail ID. The app renders supported payloads as Info.
- The audit found that process startup through the messaging service could drop callback-delivered messages before an Activity restored the session. Numeric customer IDs could overlap admin IDs. A token rotation could race a pending registration confirmation. Settings reported app permission without checking whether the Info channel was blocked. Release had no signing setup.

## Changes

- Local FCM delivery can use a valid saved session on the service worker thread, without a network refresh. Local delivery and logout use the same lock so a service callback cannot post after logout has cleared the tray. Known customer queue payloads are excluded from admin callback delivery.
- Token rotation invalidates pending registration confirmations. Logout and credential rejections that clear the session attempt bounded Firebase token deletion. The existing backend can reassign the same token on registration, but cannot disassociate it on logout.
- Settings updates the Info channel status on resume. Release signing can use an approved existing keystore via four environment variables. Signing configuration requires a release HTTPS URL and Firebase configuration.
- Startup and notification performance review found bounded registration retry on session, connectivity, and token change, no continuous polling for notifications, and local database work off the main thread. Startup, memory, and battery performance were not profiled.

## Test matrix

| Test ID | Feature | Test Scenario | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC-001 | Auth | Valid admin login | Dashboard opens | ADB unavailable and authenticated endpoint not exercised | BLOCKED |
| TC-002 | FCM | Firebase init, token registration, restart and token refresh | Backend accepts token and Settings reflects registration | Package matches local Firebase file. Device, backend registration and token rotation unverified | BLOCKED |
| TC-003 | FCM | Real foreground notification, repeat delivery | One tray entry and one cached item | No authorized admin test delivery executed | BLOCKED |
| TC-004 | FCM | Home, then notification-only and mixed delivery | Firebase displays the message when allowed | No device test executed | BLOCKED |
| TC-005 | Navigation | Tray tap while signed in or signed out, cold and warm launch | App-created tap opens the correct detail after auth | Device navigation unverified. JVM target-validation tests passed | BLOCKED |
| TC-006 | History | Open, refresh and reopen offline | Latest local rows and read state persist | SQLite and UI behavior unverified on device | BLOCKED |
| TC-007 | Security | Logout, switch admins, re-register | Old data hidden and device associated with current admin | Device flow and best-effort Firebase cleanup unverified. No server unregister route | BLOCKED |
| TC-008 | UI | Light, Dark, System, restart, large fonts and different screen sizes | All screens and system bars remain readable | Source preserves the existing theme. No visual device verification | BLOCKED |
| TC-009 | Auth | Invalid credentials, timeout, outage, expired session, API 401 and restart | Clear recovery feedback or reauthentication | JVM DTO tests passed. HTTP and Keystore flows unverified | BLOCKED |
| TC-010 | FCM | Normal process death, tray tap and session restoration | Eligible message displayed and supported tap restores navigation | No device test executed | BLOCKED |
| TC-011 | FCM | API 33+ permission allow and deny, Settings changes, Info channel blocked | App usable and tray respects OS settings | Permission and channel changes unverified on device | BLOCKED |
| TC-012 | Center | Sort, read/unread, counts, severity, filters, empty/loading/errors, refresh | Accurate list and usable state feedback | JVM filters and unread counts passed. UI and SQLite unverified | BLOCKED |
| TC-013 | JVM | Payload mapping, malformed input, stable IDs and account-scoped tap validation | Parsing, dedup key and target validation correct | Existing JUnit tests executed directly with Kotlin 2.4.21 and JUnit 4.13.2 | PASS |
| TC-014 | JVM | Auth DTO conversion and incident filters | Contract conversion and filters pass | All three test classes executed, 13 passing tests total | PASS |
| TC-015 | Build | Clean debug build, Gradle unit test task and lint | Debug APK, Gradle test and lint results | Gradle stopped during startup with local socket permission failure even with writable copied caches | BLOCKED |
| TC-016 | Release | Build, verify signing, install and launch release | Approved signed release works on-device | Gradle startup blocked, approved signing inputs unavailable, no release APK | BLOCKED |
| TC-017 | Stability | Profile startup, memory and battery, inspect fatal logs | Runtime metrics and logs captured | No device run | NOT TESTED |
| TC-018 | FCM | Data-only probe | Handling matches the deployed sender contract | No admin data-only contract identified. App ignores data-only messages | NOT TESTED |
| TC-019 | Firebase config | Check the local app package mapping | Exactly one client for `id.casan.monitor` | Local JSON check found one matching client | PASS |
| TC-020 | Kotlin | Compile Android Kotlin sources independently | Kotlin compilation completes | Direct Kotlin and Compose compiler invocation against cached dependencies and previously generated Android classes completed. Android resource generation, Hilt annotation processing, and packaging require Gradle | PASS |

## FCM delivery and release risks

- **P0 — Admin delivery and live authentication unverified.** Obtain access to an authorized test device, CASAN admin account, and deployed test sender that targets the registered admin installation. A Firebase Console message can isolate Firebase connectivity when authorized, but cannot establish CASAN backend delivery.
- **P0 — Signed APK unavailable.** Run the release build with an approved keystore, deployment URL and matching Firebase file. Confirm the signer fingerprint against the approved fingerprint and install the signed build separately.
- **P1 — Background notification history and detail.** Firebase displays notification-only and mixed messages in the background. Their launcher intents lack the app-created local ID, so they may open the normal entry point or Alerts without a local row. App-created notifications can carry a detail target. Data-only messages are ignored pending a deployed contract. Local history cannot recover notifications that never reached the callback. Redeliveries without an FCM message ID cannot be deduplicated reliably. [Firebase Android delivery behavior](https://firebase.google.com/docs/cloud-messaging/android/receive-messages)
- **P1 — Logout cleanup.** Backend unregistration is unavailable. If best-effort token deletion fails, the old token may still receive Android-managed notifications. Local session checks do not control Firebase's automatic tray display. Verify offline logout and account switching with the existing sender before the pilot.
- **P1 — Device coverage and lint outstanding.** Normal-priority delivery can be deferred during Doze. Battery restrictions can affect timing. For process death, use `adb shell am kill id.casan.monitor` after pressing Home and confirm the process exited. Explicit force-stop is a separate case. Reopen the app before expecting delivery again. [Android Doze and App Standby](https://developer.android.com/training/monitoring-device-state/doze-standby)

## Build, install and test

Use Android Studio's JetBrains JDK 25 for Gradle with the SDK components in the README. The app targets JVM 17. From the Android repository:

```sh
./gradlew clean assembleDebug :app:testDebugUnitTest :app:lintDebug
adb devices -l
adb reverse tcp:8080 tcp:8080 # Use only with the loopback debug backend.
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n id.casan.monitor/.MainActivity
adb logcat -s AndroidRuntime:E FirebaseMessaging:I
```

Supply `CASAN_RELEASE_KEYSTORE`, `CASAN_RELEASE_STORE_PASSWORD`, `CASAN_RELEASE_KEY_ALIAS`, and `CASAN_RELEASE_KEY_PASSWORD` through the local secret environment. Supply the approved HTTPS deployment URL as `casanBackendUrl` in user Gradle properties. The expected signed output is `app/build/outputs/apk/release/app-release.apk`.

```sh
./gradlew assembleRelease
apksigner verify --verbose --print-certs app/build/outputs/apk/release/app-release.apk
adb install -r app/build/outputs/apk/release/app-release.apk
adb shell am start -n id.casan.monitor/.MainActivity
```

Use a separate test device or Android profile for release testing if the debug installation uses a different signer. Start with a valid admin session and confirm **Device registered** under Settings. Send a recognizable test-only message to the registered test installation through the authorized CASAN sender. Repeat while foregrounded, after Home, and after normal background process death. Verify tray, list, tap, and unread state independently. Repeat with permission denied and the Info channel disabled. For an app-created tap, verify the target after signing out and signing in as the original and a different admin. Keep full tokens and credentials out of logs and QA evidence.
