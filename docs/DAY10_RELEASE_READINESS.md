# Day 10 release readiness

**RELEASE BLOCKED.** There is no approved signed candidate, successful device install or upgrade, verified production configuration, or verified backend-to-admin FCM delivery. No APK was distributed and no deployment or production action was performed.

| Gate | Evidence |
|---|---|
| Candidate | Source package `id.casan.monitor`, version 0.1.1, code 5 |
| Standalone checks | 24 JVM tests passed. Kotlin and Compose source compiled against cached dependencies and pre-generated Android classes |
| Gradle build and lint | Debug and release commands stopped before task execution. `FileLockContentionHandler` could not open a local socket |
| Signing and checksum | No approved signer inputs or fresh APK. No candidate checksum available |
| Historical APK | Package, launcher, app icon and Android Debug v2 signature verified on version code 1 only |
| Production configuration | Local app package and project comparison passed. Production HTTPS base and deployed sender project unverified |
| Device, login and FCM | ADB local listener failed. No staging or production account, authorized admin sender trigger, installation, upgrade or FCM result |
| Security and handover | Source review, install procedure and recovery guidance complete. Binary and device security gates remain open |

## Required sign-off

1. Release Engineering confirms production environment, previous approved APK and original signer, then generates and verifies a fresh artifact on an authorized host and records the exact signer and SHA-256.
2. Backend and Android owners validate the existing admin sender and UAT accounts. Run the device matrix against authorized staging first and run production smoke tests only with explicit approval.
3. Verify install, in-place upgrade and cross-account isolation. Resolve or explicitly gate the stale FCM association and background detail/history risks before approval.
4. Publish the exact verified binary and checksum through the approved restricted channel with [release notes](DAY10_RELEASE_NOTES.md). Retain the previous known-good build and test a higher-code recovery path.
