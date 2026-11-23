# Day 7 security review

## Authentication and API

- Admin sign-in uses the existing backend. Password state is in memory and cleared on submission. No inspected source persists the password.
- The access and refresh tokens are stored in a no-backup AtomicFile encrypted with Android Keystore AES-GCM. The persisted session must match the backend URL and admin principal. Notification cache queries include the backend and admin ID. Cached alert content uses app-private SQLite without additional encryption.
- Protected UI waits for the authenticated session, and the server validates registration's bearer token and admin roles. Registration 401 clears the local session. Registration 403 is surfaced as a registration failure. Rejected refresh credentials clear the session. Network failures surface recoverable messages. Live HTTP and Keystore behavior were not verified on device.
- Release disallows cleartext and requires an HTTPS URL when signing. The debug network configuration permits loopback HTTP. Debug endpoint selection now defaults to loopback and requires an explicit override for staging. OkHttp disables redirects and automatic retry, sets a 15-second call timeout, and has no logging interceptor. Existing TLS trust behavior was retained.
- No tracked Android Firebase JSON, keystore, or private-key marker in the inspected Android source was found. Signing credentials are read from the environment. The Firebase package/project comparison exposed no token or service-account credential.

## FCM and Android components

- Token registration uses the admin bearer token. The local persisted registration marker uses a token fingerprint and environment/account ownership. Token changes invalidate stale confirmations. Actual registration, rotation and account switching still require device UAT.
- Logout clears local credentials and registration, cancels the tray, and attempts Firebase token deletion and backend refresh-token revocation. The backend exposes no admin device-unregistration route. Failed token deletion or offline logout may leave the previous recipient reachable through Firebase's automatic background display. This remains a P1 pilot risk. Use non-sensitive message bodies during UAT and verify account switching and offline logout on device.
- The messaging service is non-exported in the source manifest. The launcher Activity is exported. App-generated notifications use immutable PendingIntents. Navigation validates a fixed-format local ID and scopes detail lookup to the signed-in admin. Incoming title/body are treated as display text and missing or blank body is rejected. The system-managed background path can display content before the app applies account checks.
- App-rendered notifications use private lock-screen visibility. Firebase-rendered background notification content depends on sender payload and Android settings. Review what staging sends before device UAT. [Firebase Android receive behavior](https://firebase.google.com/docs/cloud-messaging/android/receive-messages)
- Runtime permission testing, merged release manifest inspection, binary scanning of a fresh artifact, lock-screen behavior and deployed credentials remain unverified.
