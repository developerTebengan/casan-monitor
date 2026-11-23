# Day 8 pilot test cases

Device scenarios below target authorized staging accounts and Android hardware. ADB failed to start, and no authorized staging admin trigger was available. Record build/version, Android model and API, anonymized tester role, connection and timeline for actual device runs. Use neutral test payloads and record a safe reference or provider response without secrets.

| ID | Category | Scenario | Expected result | Actual result / evidence | Environment | Status | Notes |
|---|---|---|---|---|---|---|---|
| D8-001 | Installation | Fresh APK installation | Launch, setup and settings work | No Day 8 APK or device install | Android | BLOCKED | Gradle and ADB startup errors |
| D8-002 | Upgrade | Upgrade existing APK | Preserve session, cache and preferences | Older code-1 debug APK inspected. No candidate upgrade attempted | Android | BLOCKED | Approved previous signer and fresh build needed |
| D8-003 | Login | Valid authentication | Dashboard opens | No live staging sign-in | Staging | BLOCKED | API URL and authorized account unconfirmed |
| D8-004 | FCM | Device registration | Backend accepts current admin token | Local config comparison only | Staging / Android | BLOCKED | Check server and Settings separately |
| D8-005 | FCM | Foreground | Tray and Alerts update | No backend delivery executed | Staging / Android | BLOCKED | Check safe reference and unread |
| D8-006 | FCM | Background | Tray receives and tap opens supported destination | No backend delivery or tap executed | Staging / Android | BLOCKED | System-rendered payload may lack detail |
| D8-007 | FCM | Locked screen | Content follows privacy settings | No device lock-screen check | Android | BLOCKED | Use a neutral test message |
| D8-008 | FCM | Normal process termination | Supported delivery and navigation | No process kill or receipt | Android | BLOCKED | Press Home before `adb shell am kill id.casan.monitor` |
| D8-009 | FCM | Distinct notifications | Newest first, distinct records and correct unread | No message sequence executed | Staging / Android | BLOCKED | Current contract uses Info |
| D8-010 | FCM | Duplicate delivery | One cache row and app-managed tray entry | Stable-ID JVM coverage. No repeated delivery | Staging / Android | BLOCKED | Same FCM ID needed |
| D8-011 | Account | Same user, multiple devices | Separate registration and expected targeting | No multi-device run | Staging / Android | BLOCKED | Confirm sender recipient policy |
| D8-012 | Account | Different users | Correct recipients and isolated history | No multi-user run | Staging / Android | BLOCKED | Use two authorized admins |
| D8-013 | Account | Switch user | Old history hidden and new registration active | Diagnostic state isolation covered in JVM. No account switch | Staging / Android | BLOCKED | Include logout offline |
| D8-014 | Account | Logout on one device | Local state clears; other device follows backend policy | No logout device run | Staging / Android | BLOCKED | Server unregister absent |
| D8-015 | Alerts | List synchronization | Inserts update list and unread | In-memory repository regression, no Android SQLite run | Android | BLOCKED | Verify gesture and reconnect |
| D8-016 | Detail | Open and read alert | Correct detail, long-body layout and missing-row recovery | Target routing tested under JVM. No device UI execution | Android | BLOCKED | Confirm cached detail and Alerts fallback |
| D8-017 | Actions | Acknowledge | Confirmed status update if supported | No checked-in API contract | Staging | BLOCKED | Control remains hidden |
| D8-018 | Actions | Resolve | Confirmed status update if supported | No checked-in API contract | Staging | BLOCKED | Control remains hidden |
| D8-019 | Offline | Network loss | Valid account can read saved notifications | No network interruption executed | Android | BLOCKED | Verify valid and expired sessions |
| D8-020 | Offline | Network restoration | Local reload without duplicates | No reconnect executed | Android | BLOCKED | Offline push delivery can vary |
| D8-021 | Security | Unauthorized data access | Protected detail denied | JVM scoped target validation only | Staging / Android | BLOCKED | Verify cross-account tap and lock screen |
| D8-022 | UI | Light Mode | Readable screens and diagnostics | No visual run | Android | BLOCKED | Include small screens and large fonts |
| D8-023 | UI | Dark and System themes | Readable screens and persisted selection | No visual run or restart | Android | BLOCKED | Include permission and offline states |
| D8-024 | Stability | Repeated restart | No crashes and correct restore | No device logs collected | Android | BLOCKED | Check `adb logcat -s AndroidRuntime:E` |
| D8-025 | Release | Signed RC verification | Approved signature and verified install | Signing inputs absent and build failed at startup | macOS Intel / Android | BLOCKED | No Day 8 distribution artifact |
| D8-026 | FCM | Explicit force-stop | Document observed behavior and reopen | No force-stop executed | Android | NOT TESTED | Separate from normal process death |
| D8-027 | Performance | Soak, Battery Saver and startup | Record timing, memory and crash evidence | No device runtime measurements | Android | NOT TESTED | Preserve default battery protections |
| D8-028 | JVM | Diagnostics and notification regressions | All assertions pass | 19 JUnit tests passed, including debug account isolation, reset and uncached target fallback | macOS Intel / cached Kotlin compiler | PASS | No live FCM or Android SQLite run |
| D8-029 | Artifact | Inspect older debug baseline | Identify package, version and signer | SDK tools read version code 1 and validated the Android Debug v2 signature | macOS Intel / Android SDK | PASS | This is not the prior authorized release signer |
| D8-030 | Build | Compile Android Kotlin and Compose source | Standalone compilation succeeds | Compiler exited successfully with cached dependencies and previously generated Android classes | macOS Intel | PASS | Packaging and lint remain blocked |

## Running the pilot

On an authorized build host, configure the confirmed staging debug endpoint and Firebase file. Build with `./gradlew clean assembleDebug :app:testDebugUnitTest :app:lintDebug`. Check devices with `adb devices -l`. Install the fresh build using `adb install -r app/build/outputs/apk/debug/app-debug.apk` and launch with `adb shell am start -n id.casan.monitor/.MainActivity`. Use a clean baseline device/profile for fresh install and keep the previous app data intact on the upgrade device. Build release separately with the approved signing inputs and inspect its signer and version before testing upgrade.

Record the backend send outcome, device tray outcome, diagnostics, and detail outcome independently. Run foreground, Home/background, locked screen and normal process death. Repeat with notification permission or Info disabled and with network reconnect. Confirm intended recipient behavior on a second device before evaluating logout and account switching.
