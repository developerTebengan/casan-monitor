# Day 7 staging UAT

Use a confirmed staging admin account, approved test device, staging HTTPS debug URL and an authorized backend trigger for a single admin registration. Use distinct non-sensitive titles. Capture times, outcome, environment and message ID without capturing tokens or credentials.

| ID | Category | Scenario | Expected | Actual and evidence | Status |
|---|---|---|---|---|---|
| D7-001 | Authentication | Valid staging login | Dashboard opens | Staging URL and account unavailable | BLOCKED |
| D7-002 | Authentication | Invalid credentials | Actionable error and signed-out state | No staging authentication request executed | BLOCKED |
| D7-003 | Authentication | Session restoration | Valid account resumes | No device restart executed | BLOCKED |
| D7-004 | Authentication | Logout | Protected screens and tray clear | No on-device logout executed. Backend unregister absent | BLOCKED |
| D7-005 | FCM | Initial registration | Backend accepts admin token | Local project/package match only | BLOCKED |
| D7-006 | FCM | Foreground notification | Tray and Alerts update | No staging admin trigger or ADB access | BLOCKED |
| D7-007 | FCM | Background notification | Supported payload reaches tray | No Day 7 device delivery | BLOCKED |
| D7-008 | FCM | Notification tap | App-created tap resolves correct detail | No Android tap executed | BLOCKED |
| D7-009 | FCM | Token refresh | New token registers for current admin | No rotation executed | BLOCKED |
| D7-010 | FCM | Duplicate delivery | One row and app-generated tray item | Stable-ID JVM coverage only | BLOCKED |
| D7-011 | FCM | Permission denied or Info disabled | Tray suppressed and app usable | No permission changes executed | BLOCKED |
| D7-012 | Lifecycle | Background process recreation | Supported delivery and safe navigation | No process kill or relaunch executed | BLOCKED |
| D7-013 | Offline | Network disconnected | Valid admin can open saved rows | No airplane mode scenario executed | BLOCKED |
| D7-014 | Offline | Network restored | Reload finishes without duplicates | No transition executed | BLOCKED |
| D7-015 | API | Backend unavailable | Recoverable error and bounded retry | No outage simulated | BLOCKED |
| D7-016 | Security | Expired session | Refresh or reauthenticate before access | No device expiry test executed | BLOCKED |
| D7-017 | Security | Deep-link authentication | Protected destination gated and scoped | Target-validation JVM tests only | BLOCKED |
| D7-018 | Security | Account switch | Old history hidden and recipient changes | No account switch executed | BLOCKED |
| D7-019 | UI | Light Mode | Screen contrast and system bars correct | No device rendering | BLOCKED |
| D7-020 | UI | Dark Mode | Screen contrast and system bars correct | No device rendering | BLOCKED |
| D7-021 | UI | System Theme | Follows OS and persists | No theme toggle or restart executed | BLOCKED |
| D7-022 | Performance | Long history and large fonts | Responsive scrolling and readable text | No runtime trace or layout review | BLOCKED |
| D7-023 | Stability | Repeated foreground/background and lock/unlock | No fatal errors or repeated registration | No transitions executed | BLOCKED |
| D7-024 | Build | Debug APK and lint | Fresh version-code-2 debug artifact | Gradle startup socket error. Existing APK is version code 1 | BLOCKED |
| D7-025 | Release | Signed candidate | Approved signer and verified install | Gradle startup socket error and no approved signing values | BLOCKED |
| D7-026 | Battery | Normal, Saver, Doze and extended background | Record delay and resume behavior | No power-state measurements taken | NOT TESTED |
| D7-027 | Lifecycle | Explicit force-stop | Document behavior separately, then reopen | No force-stop executed | NOT TESTED |
| D7-028 | Stability | Multi-hour soak | Capture delays, memory, crashes and battery | No sustained run executed | NOT TESTED |
| D7-029 | FCM | Distinct messages | Unique rows and newest-first order | No multiple-message device sequence executed | BLOCKED |
| D7-030 | Firebase | Local configuration comparison | Match staging project and app package | Local values matched with one Android package client | PASS |
| D7-031 | JVM | Existing auth, FCM mapping and list regressions | All tests pass | Standalone Kotlin/JUnit ran 16 tests across four classes, all passed | PASS |
| D7-032 | Build | Compile Kotlin and Compose sources | Source compilation succeeds | Standalone compiler exited successfully against cached dependencies and existing generated Android classes | PASS |

## Physical-device procedure

1. On a host with working Gradle and authorized ADB, configure `casanDebugBackendUrl` and build with `./gradlew clean assembleDebug :app:testDebugUnitTest :app:lintDebug`. Confirm package and version with SDK tools. Install the fresh build using `adb install -r app/build/outputs/apk/debug/app-debug.apk`, then launch with `adb shell am start -n id.casan.monitor/.MainActivity`.
2. Verify Settings shows Device registered. Use the existing staging sender for foreground, Home/background, and normal process-death probes. For normal process death, press Home then run `adb shell am kill id.casan.monitor` and check `adb shell pidof id.casan.monitor`. Run explicit force-stop separately and reopen before expecting further delivery.
3. Observe tray, list, unread count and navigation separately. Repeat with permission denied and the Info channel blocked. Keep message IDs and timestamps with the test outcomes.
4. Record offline and reconnect behavior, UI states, cold and warm startup, and stability through rotation and account switching. Monitor fatal errors via `adb logcat -s AndroidRuntime:E`. For an extended staging soak, record process restarts, process memory, battery modes, duplicates, and missed or delayed messages. Use the default battery protections and record the timing under each state. [Android Doze test guidance](https://developer.android.com/training/monitoring-device-state/doze-standby)
