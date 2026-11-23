# Day 9 release test cases

Production actions were not authorized or executed. Device cases await a verified signed APK and an authorized test device. JVM cases use local update fixtures and Android data or logic regressions.

| ID | Category | Scenario | Expected | Actual / evidence | Status |
|---|---|---|---|---|---|
| D9-001 | Build | Release build | Assemble fresh release | Gradle local file-lock socket denied before configuration | BLOCKED |
| D9-002 | Security | APK signature | Verify approved signer and continuity | No approved key inputs or Day 9 APK | BLOCKED |
| D9-003 | Installation | Fresh install | Launch approved release APK | ADB local listener unavailable | BLOCKED |
| D9-004 | Upgrade | Upgrade from previous APK | Same package, signer, retained data | No approved signed baseline or candidate tested | BLOCKED |
| D9-005 | Config | Correct production backend | Approved HTTPS API base in APK | No production base confirmed and no binary to inspect | BLOCKED |
| D9-006 | Config | Firebase project | Match deployed production sender | Local JSON package and local project match only | BLOCKED |
| D9-007 | Auth | Production login | Valid login succeeds, invalid credentials rejected | No authorized login executed | BLOCKED |
| D9-008 | FCM | Token registration | Backend accepts admin registration | No production registration | BLOCKED |
| D9-009 | FCM | Foreground | Tray and Alerts update once | No authorized message sent | BLOCKED |
| D9-010 | FCM | Background | Tray receives supported payload | No authorized message sent | BLOCKED |
| D9-011 | FCM | Deep link | Correct authenticated navigation | JVM target checks only, no tap executed | BLOCKED |
| D9-012 | Notification | Alert List | Refresh, search and read state work | JVM repository/filter checks, no Android UI run | BLOCKED |
| D9-013 | Notification | History | Saved history and offline cache work | No Android device/SQLite run | BLOCKED |
| D9-014 | Security | User switching | Private history and recipient isolation | No authorized device switch/logout | BLOCKED |
| D9-015 | Update | Installed version detection | Settings shows running version and code | Source updated, no installed candidate | BLOCKED |
| D9-016 | Update | Newer version | Correct optional/required policy and no downgrade | JVM fixtures verified numeric ordering and minimum boundaries | PASS |
| D9-017 | Update | Invalid metadata | Unable to Check with no required update | JVM fixtures rejected invalid values and mapped I/O failure | PASS |
| D9-018 | Update | Offline | Distinct Offline outcome | JVM fixture returned Offline | PASS |
| D9-019 | Update | APK integrity | Verify checksum and package/signer | No Day 9 APK or downloader. Manual flow documented | BLOCKED |
| D9-020 | UI | Light Mode | Screens readable | No Android visual run | BLOCKED |
| D9-021 | UI | Dark/System Mode | Themes readable and persist | No Android visual run | BLOCKED |
| D9-022 | UI | Settings | Shows support version and permission state | No Android UI runtime | BLOCKED |
| D9-023 | Recovery | Session restore and expiry | Restore valid session, protect expired session | No restart or expiry run | BLOCKED |
| D9-024 | Recovery | In-place hotfix compatibility | Higher code and preserved app data | No hotfix candidate or install | BLOCKED |
| D9-025 | Release | Readiness review | Record evidence and decision | Reports completed with RELEASE BLOCKED decision | PASS |
| D9-026 | Update | Link validation and installer consent | Trusted HTTPS page and system approval | No approved link or live integration to exercise | NOT TESTED |
| D9-027 | Build | Debug APK, Gradle unit tests and lint | Build and reports complete | Same local socket permission error before configuration | BLOCKED |
| D9-028 | JVM | All unit regressions | Tests execute successfully | Standalone JUnit ran 24 tests with zero failures, including five update-policy tests | PASS |
| D9-029 | Build | Standalone Kotlin and Compose source compilation | Compiler exits successfully | Compiled against cached dependencies and previously generated Android classes | PASS |
