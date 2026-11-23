# CASAN Monitor 0.1.1 (5) candidate notes

**Release blocked.** This identifies the Day 10 source candidate. An approved signed APK, checksum and publication date are pending.

- Admin login and session restoration use the existing CASAN backend.
- Dashboard, Alerts, History and notification detail use locally received notifications. Search, severity filters, read/unread status, local refresh and same-account offline access are supported in the existing implementation.
- App-created notifications can open saved detail. Storage-failure taps fall back to Alerts. Debug Settings provides delivery diagnostics.
- Settings supports Light, Dark and System themes and displays app version and Android API level.
- Numeric version policy is available for future integration with an approved source. Updates currently follow the manual internal distribution procedure.

The app does not have a server-wide incident history or acknowledge/resolve contract. Current supported payloads appear as Info. Android-managed background delivery can lack a local history row and detail link. See [release gates](DAY10_RELEASE_READINESS.md) before issuing these notes with an artifact.
