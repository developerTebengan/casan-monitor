# Day 9 implementation report

## Audit

Android started clean on `stg` at `454d08b`. Reviewed the Day 7 security findings and Day 8 pilot, release, and diagnostics reports. Day 8 had no approved signed APK, on-device install/upgrade, or verified CASAN admin notification delivery. Backend remained unchanged on `feature/monitor`.

Debug and release already use separate endpoint properties. Build source uses `id.casan.monitor` and the existing release signing settings. Release transport requires HTTPS when configured. The source manifest has cleartext disabled, a non-exported Firebase service, the exported launcher, and the existing notification permission. The inspected OkHttp client has no logging interceptor. Day 8 receipt diagnostics remain debug-only. Android signing and Firebase config files are untracked. No production APK was available for binary inspection.

The local `google-services.json` has exactly one matching Android client. Its project ID matches the local backend staging and default configuration files. The deployed production sender project, production HTTPS API base, approved signer, and authorized smoke-test trigger were unavailable. A project-ID match in local files cannot establish environment separation or prove that a deployed sender can address this installation.

No approved release metadata source or internal distribution URL was identified in repository routes, docs, or configuration. Existing backend admin sign-in, refresh, logout and authenticated Firebase registration contracts remain in use. The checked-in push send call sites target customer tokens. Admin unregistration and incident action APIs remain unavailable.

## Changes

- Bumped version code from 3 to 4 and version name from 0.1.0 to 0.1.1 in `app/build.gradle.kts`.
- Added `domain/update/UpdateChecker.kt` with a metadata source interface and numeric version policy. It distinguishes up to date, optional update, required update, offline and unable to check. Invalid policy and transport failures never imply a required update. The integration remains unwired pending an approved metadata source.
- Added five fixture-based tests in `domain/update/UpdateCheckerTest.kt`, including boundaries, invalid metadata, offline and I/O outcomes, and caller cancellation.
- Updated `SettingsScreen.kt` to show version name, version code and Android API level. Existing registration and permission status and debug-only diagnostics remain in place.
- Added the five Day 9 documents. Backend, notification routes, dependencies, database schema and installer permissions were unchanged.

## Issues and verification

| Severity | Finding | Status |
|---|---|---|
| P0 | Production configuration, signing, installation and admin delivery unverified | Release gate blocked |
| P1 | No device unregistration on logout | Token deletion remains best effort. Stale background deliveries remain possible |
| P1 | System-managed FCM can lack local history and a detail route | Existing sender contract limitation |
| P2 | No approved update metadata or distribution source | Implemented policy and fixtures. Live integration pending |
| P3 | Release support needed a visible build code and OS level | Settings source updated |

Standalone Kotlin and Compose source compilation succeeded against cached dependencies and previously generated Android classes. All 24 JVM tests passed. Gradle debug and release attempts stopped before task execution at `FileLockContentionHandler` due to local socket permissions. ADB failed to bind its local listener. No production smoke test or distribution was authorized or attempted. See the release test matrix for execution evidence and the readiness report for outstanding gates.
