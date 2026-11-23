# Day 10 final regression checklist

The matrix tracks release scenarios independently from the standalone JVM suite. Production actions were not authorized or executed. No authorized staging trigger or device access was available.

| ID | Category | Scenario | Expected | Actual and evidence | Environment | Status |
|---|---|---|---|---|---|---|
| D10-001 | Build | Debug build and lint | Fresh debug APK and reports | Gradle local lock socket denied before configuration | macOS Intel | BLOCKED |
| D10-002 | Build | Release build | Fresh release package | Same Gradle startup failure | macOS Intel | BLOCKED |
| D10-003 | Security | APK signature | Approved signing certificate | No candidate or approved signing inputs | Android SDK | BLOCKED |
| D10-004 | Installation | Fresh install | Installed and launched | ADB could not bind listener | Android | BLOCKED |
| D10-005 | Installation | Upgrade | Preserve session, cache and preferences | No candidate or prior authorized signed baseline | Android | BLOCKED |
| D10-006 | Auth | Valid login | Dashboard opens | No authorized live request | Staging/production | BLOCKED |
| D10-007 | Auth | Invalid credentials | Actionable error and no protected screen | No authorized live request | Staging/production | BLOCKED |
| D10-008 | Auth | Session persistence, expiry and 401/403 | Restore or reauthenticate as applicable | Device and backend responses not exercised | Android/staging | BLOCKED |
| D10-009 | Auth | Logout | Local session and tray clear | No device execution. Backend unregister is absent | Android/staging | BLOCKED |
| D10-010 | Firebase | Token registration and refresh | Backend accepts device association | Local Firebase configuration checked only | Android/staging | BLOCKED |
| D10-011 | Firebase | Foreground | Tray and Alerts update once | No authorized send or device callback | Android/staging | BLOCKED |
| D10-012 | Firebase | Background | Tray displays supported payload | No authorized send or device receipt | Android/staging | BLOCKED |
| D10-013 | Firebase | Locked screen | Privacy controls content | No locked-screen test | Android | BLOCKED |
| D10-014 | Firebase | Deep link | Correct scoped detail or supported fallback | JVM routes covered. No tray tap executed | Android | BLOCKED |
| D10-015 | Firebase | Duplicate prevention | Single row for same stable ID | JVM identity checks only. SQLite/tray untested | Android | BLOCKED |
| D10-016 | Alerts | Dashboard and list | Correct order, counts, retry and refresh | JVM list checks only. Screen unavailable | Android | BLOCKED |
| D10-017 | Alerts | Search and filters | Matching results and empty state | JVM filters covered. No UI run | Android | BLOCKED |
| D10-018 | History | Cached history | Order, date grouping and read state persist | No Android SQLite run | Android | BLOCKED |
| D10-019 | Detail | Notification detail | Saved body and available metadata show | No UI run. Source and severity absent from current contract | Android | BLOCKED |
| D10-020 | Incident | Acknowledge if supported | Authorized action | No backend contract found | Backend | BLOCKED |
| D10-021 | Incident | Resolve if supported | Authorized action | No backend contract found | Backend | BLOCKED |
| D10-022 | Offline | Offline cache | Valid admin sees own saved records | No airplane-mode run | Android | BLOCKED |
| D10-023 | Offline | Network recovery and outage | Bounded retry and usable cache | Network transitions and outage simulation unavailable | Android/staging | BLOCKED |
| D10-024 | UI | Light and small-screen layout | Readable and usable | No rendering run | Android | BLOCKED |
| D10-025 | UI | Dark and large fonts | Readable and usable | No rendering run | Android | BLOCKED |
| D10-026 | UI | System theme | Tracks setting and persists | No toggle or restart | Android | BLOCKED |
| D10-027 | Update | Version policy in isolation | Valid comparison and offline/error outcomes | Fixture assertions passed. Live UI/source absent | JVM | PASS |
| D10-028 | Security | Account switching | Isolated cache and authorized recipient | No live two-account test | Android/staging | BLOCKED |
| D10-029 | Performance | Large history and lifecycle | Bounded list and responsive navigation | No cold/warm, rotation, restart or profiling measurement | Android | NOT TESTED |
| D10-030 | Release | Final readiness review | Evidence reviewed and disposition recorded | Readiness report completed with RELEASE BLOCKED | Repository | PASS |
| D10-031 | Firebase | Process death | Supported delivery and restore | No Home/normal process-kill run | Android | BLOCKED |
| D10-032 | Firebase | Several notifications and malformed data | Distinct rows and safe invalid-input handling | JVM parser/ID checks. No device sequence | Android | BLOCKED |
| D10-033 | Android | Permission, channel and Battery Saver | Respect system settings | No device settings or power-state checks | Android | BLOCKED |
| D10-034 | Config | Local Firebase mapping | Single package and matching local project | Package and both local project comparisons matched | Local config | PASS |
| D10-035 | Build | Standalone Kotlin and Compose | Source compilation completes | Compiler succeeded using cached dependencies and prior generated Android classes | macOS Intel | PASS |
| D10-036 | JVM | Existing regressions | All unit assertions pass | 24 tests executed, zero failures | Kotlin/JUnit | PASS |
| D10-037 | Artifact | Inspect historical debug APK | Read package, icon, launcher and signer | SDK tools verified code 1 and Android Debug v2 signature | Android SDK | PASS |
| D10-038 | Update | Live version, link and package consent | Approved update source and user-confirmed installation | No approved source or live installer integration | Android | BLOCKED |

Checklist totals across 38 scenarios: 6 PASS, 0 FAIL, 31 BLOCKED, 1 NOT TESTED. The separate standalone suite ran 24 JUnit tests, all passing.
