# CASAN Monitor Engineering handover

## System map

CASAN Monitor is an internal Android client for receiving and reviewing monitoring notifications. The production UI uses Compose Material 3, Hilt, ViewModels and StateFlow. `app/src/main/java/id/casan/monitor/` contains `core/` for navigation, DI and networking, `data/` for API, session and persistence, `domain/` for notification and update policy, `feature/` for screens, and `notification/` for token sync, payload handling and debug diagnostics.

The app signs in via `POST /api/admin-login`, restores sessions locally or uses `POST /api/refresh-token`, and invokes `POST /api/logout` on sign-out. The app sends its Firebase token with an admin bearer token to `POST /api/firebase/admin-token`. The backend migration `20261123_admin_firebase_tokens.sql` provides token storage. Confirm the migration and authorized sender in the target deployment before UAT.

Firebase callbacks validate account and payload, save the record to SQLite and request a tray notification where Android permits it. Tray taps for saved records open an account-scoped detail. Alerts and History are the newest 250 saved records. Older rows remain available to matching detail routes and for deduplication. Valid restored sessions can view same-account offline cache. The store is SQLiteOpenHelper schema 1. DataStore persists theme settings. Registration uses bounded in-process retries on relevant changes. There is no app-owned periodic WorkManager job.

Use the README toolchain, JetBrains JDK 25 for the Gradle daemon and Android SDK Platform 37.1, and the existing debug and release build types. Configure `casanDebugBackendUrl` for approved staging and `casanBackendUrl` for release. Install the approved ignored `app/google-services.json`. Supply `CASAN_RELEASE_KEYSTORE`, `CASAN_RELEASE_STORE_PASSWORD`, `CASAN_RELEASE_KEY_ALIAS` and `CASAN_RELEASE_KEY_PASSWORD` through the local secret environment. Never commit or print these values. Follow the [installation guide](DAY10_INTERNAL_INSTALLATION_GUIDE.md) and [update strategy](DAY9_UPDATE_STRATEGY.md).

## Troubleshooting and escalation

| Symptom | First checks | Escalation |
|---|---|---|
| Cannot log in | Confirm build environment, network, active admin role and exact error category | Backend auth owner with sanitized status and time |
| Token registration fails | Open Settings, verify Firebase project, validated connectivity and migration, then use Retry registration | Firebase/backend owner with registration outcome and environment |
| Notification never arrives | Confirm recipient registration, sender project, Android permission, Info channel and test message ID | Sender owner with acceptance result and client receipt time |
| Duplicate notifications | Compare safe references, sender event count and tray versus history | Android owner for stable ID dedup, backend owner for independent sends |
| Delayed delivery | Record send/receive timeline, priority, network, power mode and process state | Firebase/backend owner. Keep system battery protections enabled |
| Detail cannot open | Confirm active admin, message in Alerts and whether Firebase rendered the tray item | Android owner for saved-row failure. Sender owner for unavailable background detail contract |
| Offline | Check valid session and existing saved Alerts/History, then reconnect and refresh | Android owner if scoped cache or retry fails |
| APK install fails | Verify checksum, signer, package and Android version before retrying | Release Engineering |
| Upgrade fails | Compare old/new signer and numeric version. Preserve app data | Release Engineering for compatible higher-code recovery |
| Theme or UI issue | Record Android API, app version, theme, font scale, screen size and reproduction | Android owner with sanitized screenshot |

Use Engineering and backend operational owners until CASAN supplies a named support contact. Record environment, app version, time and safe reference. Keep tokens, credentials and sensitive payloads out of logs. Runtime paths are pending the [final device test matrix](DAY10_FINAL_TEST_CASES.md).
