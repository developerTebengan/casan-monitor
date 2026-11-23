# Day 7 implementation report

## Repository and staging audit

Android started with a clean worktree on `stg`, at `55a903e`. Backend review used `feature/monitor` at `be54b05`. The Day 5 and Day 6 reports were reviewed together with the manifest, network client, auth lifecycle, token registration, messaging service, channels, notification cache, list and detail screens. The existing notification store is SQLiteOpenHelper, scoped by backend and admin. DataStore persists theme preferences.

The Android Firebase config has one client for `id.casan.monitor`. Its project ID matches the local backend staging configuration. The deployed sender's project and staging API base URL remain unconfirmed. The backend's staging redirect and gateway defaults serve different purposes from the Android API base. The existing admin login, refresh, logout and Firebase registration contracts were reviewed. The repository has admin token storage and a Firebase helper, but the located sender call sites address customer tokens. No confirmed staging admin trigger was available. Standalone compilation reported deprecation warnings for the existing Firebase registration-token APIs. Retaining those APIs preserves the current backend token contract.

## Findings and fixes

| Severity | Finding | Disposition |
|---|---|---|
| P0 | Required CASAN backend-to-admin delivery and staging auth were unverified | Blocked by missing staging inputs, sender access and device access |
| P1 | Unset debug override inherited the release API URL | Debug now defaults to loopback and accepts an explicit staging override. Release still uses its separate HTTPS property |
| P1 | A canceled Firebase SDK task looked like coroutine cancellation, skipping registration retry | SDK task cancellation now surfaces as an I/O failure. Real coroutine cancellation still cancels the await |
| P1 | Failed logout token deletion can leave Android-managed notifications deliverable | Existing backend lacks admin device unregistration. See security review |
| P1 | Android-managed notifications can lack local history and a detail target | Existing payload contract limitation. The app-generated tap path remains account-scoped |
| P2 | Session expiry timer ran every minute while the Activity was stopped | Monitor expiry only while the Activity is STARTED. Resume checks and foreground expiry checks still run |
| P3 | Candidate needed a new install version | Version code advanced to 2. Version name remains 0.1.0 |

## Background and performance review

The messaging callback parses the notification, performs an account-scoped SQLite insert on its worker thread and posts the tray entry when permissions allow. Network token sync runs separately on an I/O coroutine. It reacts to account, token and validated connectivity changes and runs up to three attempts per trigger. The app does not schedule its own periodic WorkManager jobs, foreground service, or wake locks. Existing Firebase SDK background behavior remains in place.

Notification reads use a bounded newest-first window of 250 rows and UI lazy lists. The Day 6 implementation retains a loaded list when a later read fails. A valid restored session allows offline access to that admin's saved records. Expired sessions gate access pending refresh or reauthentication. Startup, scrolling, memory, excessive work, real crash frequency and battery impact were reviewed from source only and still require device profiling. Battery Saver, Doze, extended background and multi-hour soak were not executed. Doze and power restrictions can delay delivery. [Android Doze and App Standby](https://developer.android.com/training/monitoring-device-state/doze-standby)

## Files and dependencies

- `app/build.gradle.kts` separates debug endpoint selection and advances version code.
- `MainActivity.kt` and `LoginViewModel.kt` scope expiry monitoring to a started Activity.
- `FirebaseDeviceSync.kt` distinguishes SDK task cancellation from caller cancellation.
- `README.md` documents the staging debug override. Four Day 7 reports record audit, security, UAT and release status.
- Dependency versions and backend files were unchanged.
