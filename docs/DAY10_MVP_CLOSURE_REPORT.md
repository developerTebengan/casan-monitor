# CASAN Monitor MVP closure report

## 1. Executive summary

The source MVP and handover are complete for the implemented feature set. Release validation remains open. There is no Day 10 signed APK, device installation or verified CASAN backend delivery to an admin device.

## 2. Project objectives

Provide an internal Android client for authenticated monitoring notifications, local review and safe internal APK updates.

## 3. Implemented features

Admin sign-in, secure session restoration, token registration, local notification delivery, Dashboard, Alerts, History, detail, read state, search, filters, themes and debug diagnostics. An update policy is implemented with a local fixture suite.

## 4. Existing backend integrations

The Android app uses the admin login, refresh, logout and Firebase admin token routes. No checked-in admin alert sender or incident actions were confirmed. See [Engineering handover](DAY10_ENGINEERING_HANDOVER.md).

## 5. Architecture overview

Compose Material 3, Hilt, Kotlin ViewModels and StateFlow, Retrofit/OkHttp, Keystore-backed session storage, SQLiteOpenHelper cache and DataStore preferences.

## 6. FCM test results

Earlier Firebase Console receipt was user-reported. Day 10 registration, backend receipt, foreground/background delivery, lock-screen display and tap behavior remained blocked. Local ID and routing checks passed in JVM tests.

## 7. Authentication test results

DTO and contract checks ran as part of 24 passing JVM tests. Valid and invalid logins, refresh, 401/403 handling, logout and account switching were not tested live.

## 8. Notification Center test results

JVM checks covered list updates, unread counts, filtering, payload parsing and target validation. SQLite, UI rendering, persistence and offline behavior on Android remain unverified.

## 9. Security review

Source controls and remaining logout and background risks are documented in the [security review](DAY10_SECURITY_REVIEW.md). A fresh binary and live authorization results remain necessary.

## 10. Performance review

Notification queries use a bounded view and the service writes off the main thread. Refetch and registration are event-driven. Startup, scrolling, crash/ANR rate, memory, battery and soak behavior were not measured on a device.

## 11. APK build and signing results

Source candidate is 0.1.1 (5). Gradle debug/release and lint were blocked by local socket access. SDK tools verified the historical code-1 debug APK signature only. There is no candidate signature or checksum.

## 12. Installation and upgrade results

ADB could not start its local listener and no prior authorized signed baseline was supplied. Fresh install and in-place upgrade remain blocked.

## 13. Known limitations

History is local, and Android-managed FCM can omit a cached row or a deep-link target. The current payload provides no authoritative source or severity, so supported messages display as Info. Server incident workflow and a live update source are unavailable.

## 14. Unresolved issues

Approved signer and build host, production environment and admin sender validation, installation and upgrade, logout association, and verification of participant permissions.

## 15. Release recommendation

**RELEASE BLOCKED.** Use the [readiness gates](DAY10_RELEASE_READINESS.md) and [regression checklist](DAY10_FINAL_TEST_CASES.md) to reopen release validation after the prerequisites are available.

## 16. Future development recommendations

Confirm admin delivery and device deactivation with the backend owner. Verify cross-account behavior and agreement on an authoritative incident API. Wire updates only once CASAN has approved a trusted metadata and distribution source. Keep the tested higher-code rollback process and record live Android reliability evidence before broader distribution.
