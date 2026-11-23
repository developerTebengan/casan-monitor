# Day 10 security review

## Source review

- Admin login uses existing CASAN authentication. The password stays in transient UI state and is cleared on submission. Access and refresh tokens are stored in a Keystore-backed AES-GCM no-backup file. Stored credentials bind to the backend URL and admin principal.
- Retrofit sends a bearer header for admin device registration. OkHttp uses a 15-second call timeout, disables redirects and transport retries, and has no logging interceptor. Release configuration requires HTTPS for a configured API. Debug loopback has its own network configuration.
- The source manifest disables backup and cleartext traffic. The Firebase service is private and the launcher Activity is exported. App-generated taps use immutable PendingIntents and validate a scoped local ID before looking up the authenticated admin's record. Alerts are stored in app-private SQLite, scoped by admin and backend.
- The Android source and tracked-files check found no committed Firebase client JSON, keystore, or inspected PEM private-key marker. The scan does not substitute for final binary inspection. Signing secrets come from the environment. The inspected historical APK is explicitly debuggable and Android Debug signed. Release debuggability, merged permissions and embedded binary values require a fresh APK.
- Debug Settings shows bounded in-memory FCM diagnostics and short hashed references. The app does not log full tokens or payloads in the reviewed code. Lock-screen presentation depends on Android and sender behavior. [Firebase receive behavior](https://firebase.google.com/docs/cloud-messaging/android/receive-messages)

## Remaining release risks

- Logout clears local state, cancels tray entries, attempts Firebase token deletion and invokes backend refresh-token revocation. There is no backend device-unregister route. Failed token deletion can leave system-rendered background notifications deliverable after logout. This is a material P1 for rollout until the backend and mobile owners can verify and control the risk.
- System-rendered FCM notification payloads can appear before app-side account and payload checks. Use neutral authorized test messages and explicitly exercise locked-screen and cross-account behavior before release.
- Production auth, 401/403 behavior, recipient selection, fresh-signature continuity and APK configuration remain device and deployment gates. Do not infer them from source or from the historical debug APK.
