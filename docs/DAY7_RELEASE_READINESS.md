# Day 7 release readiness

**RELEASE BLOCKED.** The intended pilot requires verified CASAN staging delivery to an admin device and a signed APK installed on physical Android hardware. Neither requirement was verified in this execution.

| Gate | Result |
|---|---|
| Standalone verification | Kotlin and Compose source compilation succeeded. All 16 JVM tests passed |
| Debug build, test task, lint | Gradle stopped before configuration. `FileLockContentionHandler` could not open a local socket |
| Release build and signing | Gradle hit the same failure. Approved signing variables were absent |
| Existing artifact | SDK inspection identified a pre-existing version-code-1 debug build. Source now specifies version code 2 and version name 0.1.0. The older build provides no Day 7 evidence |
| Install and startup | ADB could not start its local listener. Device presence and launch are unknown |
| Staging | Local Firebase project IDs and Android package match. Deployed API base URL, HTTPS, credentials and admin notification trigger remain unconfirmed |
| FCM | Previous Firebase Console success was reported by the user. Day 7 staging registration, delivery and tap flow remain unverified |
| Security | Source review completed. Offline logout and automatic background delivery remain a P1 risk |

## Required evidence before pilot

1. Confirm the staging HTTPS API base URL, approved staging Firebase Android config and deployed sender project, admin test account, and existing admin push trigger.
2. Build with the staging URL in `casanDebugBackendUrl` and execute the [device UAT](DAY7_UAT_TEST_CASES.md). Log server acceptance and device receipt independently.
3. Build the release separately with the approved distribution API URL in `casanBackendUrl`, approved Firebase file and existing signing credentials. Verify package, code 2, name 0.1.0, icons, environment, signer and install. Use a separate device or profile for a different signing certificate.
4. Resolve or explicitly gate the remaining logout and background history/detail risks before approving the pilot. No APK was issued from this execution.
