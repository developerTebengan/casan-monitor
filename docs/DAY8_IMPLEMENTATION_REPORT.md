# Day 8 implementation report

## Initial audit

Android began clean on `stg` at `cdf5249`. The unchanged backend was on `feature/monitor` at `be54b05`. Reviewed all four Day 7 reports and current auth, registration, Firebase service, channels, navigation, list, detail and cache code.

| Severity | Finding | Day 8 disposition |
|---|---|---|
| P0 | Admin delivery from the deployed backend remains unverified | Checked-in sender call sites still target customer tokens. No authorized staging admin trigger and no ADB access were available |
| P1 | Cache-write failure posted a tray notification pointing to a missing detail | Fallback tap now routes to Alerts. Cached notifications retain direct, account-scoped detail navigation |
| P1 | Offline logout can leave a previous installation reachable | Backend device unregistration is unavailable. Existing token deletion remains best effort |
| P1 | Firebase-rendered background notifications can lack local history and detail | Background handling depends on the actual payload and existing sender contract |
| P2 | Debug builds could not distinguish receipt, validation, caching, posting and detail loading | Added in-memory debug diagnostics to Settings |
| P3 | New candidate needs a higher install version | Source version code is 3 and version name remains 0.1.0 |

## Implementation

Settings shows the latest account-scoped FCM callback and separately the last successfully loaded detail, with safe references and timestamps. Logout clears the snapshot. Existing registration status remains separate. App-rendered notifications retain private lock-screen visibility, existing channel handling and stable tray tags. No payload formats, channels, backend APIs or dependencies changed.

The database remains account- and backend-scoped SQLiteOpenHelper schema 1. No migration or data clearing was introduced. Alerts, History, pull-to-refresh and offline cache behavior were retained. Incident Acknowledge and Resolve remain unavailable under the checked-in contracts.

Receipt diagnostics are bounded to a single snapshot and run in the existing callback. Token retry remains bounded and event-driven. No new workers, foreground service, wake locks, network requests or periodic work were added. Source review found one cache-failure navigation defect. Runtime crash rate, ANRs, scrolling, battery and restart behavior remain unmeasured because ADB could not start.

## Files changed

- `app/build.gradle.kts` for version code.
- `MainActivity.kt`, `MonitorNavigation.kt` and `SettingsScreen.kt` for diagnostics display and screen events.
- `CasanFirebaseMessagingService.kt`, `NotificationTarget.kt` and `AuthRepository.kt` for delivery outcomes, fallback routing and sign-out cleanup.
- `NotificationDiagnostics.kt` and `NotificationDiagnosticsTest.kt` for the diagnostic state and regressions.
- Four Day 8 reports under `docs/`.
