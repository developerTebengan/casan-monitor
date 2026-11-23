# Day 6 test cases

PASS applies to the specific execution evidence shown. JVM coverage uses an in-memory repository where indicated. This environment could not start Gradle's local lock service or the ADB server. The user's earlier Firebase Console success is Day 5 evidence.

| Test ID | Feature | Scenario | Expected | Actual | Status |
|---|---|---|---|---|---|
| TC-001 | Alerts | Receive new FCM | List updates | JVM simulated repository insert updated the list and unread count. FCM delivery and SQLite integration were not exercised | BLOCKED |
| TC-002 | Incident | Open valid detail | Available details shown | Target validation passed in JVM, device navigation unavailable | BLOCKED |
| TC-003 | Incident | Acknowledge success | Confirmed status updates | No backend action contract | BLOCKED |
| TC-004 | Incident | Resolve success | Confirmed status updates | No backend action contract | BLOCKED |
| TC-005 | Offline | Disconnect internet | Account cache displayed | JVM fake returned scoped data. Airplane mode and SQLite persistence were not exercised | BLOCKED |
| TC-006 | Alerts | App returns to foreground | Reload local data | No app execution | BLOCKED |
| TC-007 | Alerts | Pull-to-refresh | Reload without clearing list | JVM explicit refresh passed. Gesture was not exercised | BLOCKED |
| TC-008 | Alerts | Network recovery | On reconnect, reload local data | No connectivity transition executed | BLOCKED |
| TC-009 | FCM | Repeat the same delivery | One record and tray entry | JVM stable-ID regression passed. SQLite and tray unavailable | BLOCKED |
| TC-010 | Detail | Missing notification | Display unavailable state | No app execution | BLOCKED |
| TC-011 | Detail | Incomplete metadata | Render available fields safely | Parsing tests passed. Device screen unavailable | BLOCKED |
| TC-012 | Detail | Copy incident reference | Copy verified reference | No incident reference contract | BLOCKED |
| TC-013 | Actions | Acknowledge failure | Explain failure | No backend action contract | BLOCKED |
| TC-014 | Actions | Resolve failure | Explain failure | No backend action contract | BLOCKED |
| TC-015 | Actions | Duplicate submission | One request | No backend action contract | BLOCKED |
| TC-016 | Actions | Unauthorized request | Backend denies action | No backend action contract | BLOCKED |
| TC-017 | Actions | Backend capability unavailable | Controls absent | Source inspection found no action controls. No device UI execution | BLOCKED |
| TC-018 | Filters | Filter by severity | Matching rows only | JVM combined-filter assertions passed | PASS |
| TC-019 | Filters | Filter by status | Matching statuses only | No status contract | BLOCKED |
| TC-020 | Search | Match title and description | Case-insensitive matches | JVM search assertions passed | PASS |
| TC-021 | Search | No results | Empty filtered collection | JVM returned empty list. Empty-state rendering was not exercised | PASS |
| TC-022 | Search | Navigate to detail and back | Keep filters | No app execution | BLOCKED |
| TC-023 | UI | Light Mode | Readable layout | No app execution | BLOCKED |
| TC-024 | UI | Dark Mode | Readable layout | No app execution | BLOCKED |
| TC-025 | UI | System theme | Follow Android | No app execution | BLOCKED |
| TC-026 | UI | Small screen | Layout fits | No app execution | BLOCKED |
| TC-027 | UI | Large font | Text stays usable | No app execution | BLOCKED |
| TC-028 | UI | Offline indicator | Shows offline status | No app execution | BLOCKED |
| TC-029 | Auth | Login | Dashboard opens | Existing DTO tests passed. No live login executed | BLOCKED |
| TC-030 | Auth | Restart | Restore valid session | No device restart executed | BLOCKED |
| TC-031 | FCM | Register Android device | Backend accepts token | Package match checked locally. No live registration executed | BLOCKED |
| TC-032 | FCM | Foreground notification | Tray and Alerts update | No Day 6 device delivery executed | BLOCKED |
| TC-033 | FCM | Background notification | Tray displays permitted message | No Day 6 device delivery executed | BLOCKED |
| TC-034 | Navigation | Notification deep link | Correct account detail | JVM account and ID validation passed. No device tap executed | BLOCKED |
| TC-035 | Auth | Logout | Session cleared and old cache hidden | No live logout executed | BLOCKED |
| TC-036 | Alerts | Observe inserts and mark read | List and unread count update | JVM fake repository emitted both updates through the list observer | PASS |
| TC-037 | Alerts | Explicit reload with two accounts | Reload only selected admin and update local refresh time | JVM saw two subscriptions for the selected account and excluded the second admin's record | PASS |
| TC-038 | Build | Gradle configuration, assemble, test, lint | Fresh debug APK and reports | Gradle stopped before configuration with a local socket permission error | BLOCKED |
| TC-039 | Install | ADB device discovery and install | Fresh debug APK installs and launches | ADB could not bind its local listener. The existing APK predates Day 6 | BLOCKED |
| TC-040 | Permissions | Allow, deny and disable Info | App remains usable and tray respects OS settings | Manifest inspected. No device permission changes executed | BLOCKED |
| TC-041 | Build | Compile main Kotlin and Compose sources directly | Compiler exits successfully | Compiler exited successfully against cached dependencies and pre-generated Android classes | PASS |
| TC-042 | UI | Calculate severity badge contrast | All eight color pairs meet 4.5:1 | Measured minimum 7.16:1 from source colors | PASS |

The standalone Kotlin/JUnit check ran all four test classes, with **16 passing tests**. Android lint, Gradle unit-test execution, packaging and installation remain outstanding. The expected fresh artifact is `app/build/outputs/apk/debug/app-debug.apk`.
