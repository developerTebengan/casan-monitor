# Day 8 release readiness

**RELEASE BLOCKED.** The intended pilot requires verified CASAN backend delivery to admin devices, account isolation during device testing, and an approved signed and installed APK. These gates remain open.

| Gate | Evidence |
|---|---|
| Debug and release assembly | Gradle failed before task execution with a local socket error in FileLockContentionHandler |
| Gradle tests and lint | Same startup error. Standalone verification is recorded in the test report |
| Signing | No approved release signing variables were present. No signed Day 8 APK was generated |
| Package and upgrade | SDK tools verified the older APK’s Android Debug v2 signature and identified `id.casan.monitor`, version code 1, version name 0.1.0 and launcher `MainActivity`. Source candidate is version code 3. Previous authorized internal signer and install/upgrade compatibility remain unverified |
| Physical devices | ADB could not bind its local listener. Device presence, install, upgrade and launch are unverified |
| Environment | One Android Firebase package client matched the local staging project. Deployed staging API and sender alignment remain unconfirmed |
| Pilot delivery and multi-user testing | No authorized admin staging trigger or device run, so no delivery, logout, account switching or multi-device recipient behavior verified |

## Day 9 release gates

1. Confirm the deployed staging HTTPS URL, test admin accounts, sender project and existing admin delivery trigger. Use two authorized test devices and non-sensitive payloads.
2. Build on a host that supports Gradle and ADB. Obtain the prior authorized internal APK and existing signing inputs. Verify package ID, increasing version code, signer compatibility and an in-place upgrade without clearing app data.
3. Verify delivery, lock-screen privacy, logout, account switching and local detail behavior using the pilot matrix. Resolve or explicitly gate the remaining background history and stale-registration risks before pilot approval. No distribution was performed.
