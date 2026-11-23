# Day 6 implementation report

## Initial audit

- Reviewed the Android sources, the Day 5 report, and backend routes, token storage and Firebase callers. The user reported a successful Firebase Console delivery before Day 6. CASAN backend delivery to an admin installation remains unverified.
- Alerts and History already observe an account-scoped SQLite database. The production path uses SQLiteOpenHelper and uses DataStore for theme preferences. Live inserts, newest-first ordering, read state, manual refresh, retained history, and secure session restoration already had implementations. No Room migration was needed.
- The UI previously replaced loaded lists with an error after a failed SQLite reload. The connectivity observer also requested a refresh for the initial connected state each time its lifecycle restarted. The database treated all insert constraint errors as duplicates.
- The manifest has a private Firebase service, a launcher Activity, notification permission and an existing default Info channel. Day 5 blockers still include an unverified admin sender, background details for platform-rendered FCM notifications, and the lack of backend unregistration on logout.

## Implemented

- Keep the current account's last successful list visible when a later SQLite reload fails while observing it. Show a retry action and retain the last successful local refresh time. A fresh observation with no successful read shows the existing load error.
- Refresh the list on connectivity recovery without treating initial connectivity as a recovery. Preserve foreground and pull-to-refresh behavior and existing FCM-driven updates. No background polling or additional HTTP requests.
- Deduplicate callback-delivered notifications using the existing unique account and FCM message identity. Check for that exact cached row on an insert constraint failure. Unexpected storage errors still reach the existing Android tray fallback.
- Remember filtered lists and history groups, add a clear-search action, and keep the existing saved filter state. Search covers title and message within the latest 250 loaded records. The history order and date grouping are retained.
- Use labeled red, orange, amber and blue severity badges with separate light and dark colors. Move the badge out of the alert title row for narrow layouts. Current payloads still display Info because no verified severity field exists.
- Present the notification title, message, and delivery information in separate detail cards. Receipt time and read state describe the notification. Incident status, source, station, device, session, error code and copy controls require fields that the current payload does not supply.
- Dashboard shows unread and recent local notification counts and the last successful local refresh. Existing Settings and CASAN brand colors are preserved.

## Backend integration and limits

The app continues to use `POST /api/admin-login`, `POST /api/refresh-token`, `POST /api/logout`, and authenticated `POST /api/firebase/admin-token`. The checked-in backend has admin token registration and an FCM helper. The sender call sites found in gateway, rental and queue services address customer tokens. No Android incident API or server-side change was added.

Missing contracts include an admin-targeted incident sender, a paginated incident list and detail, stable notification or incident identifiers, severity and incident metadata, acknowledge and resolve operations with authorized status transitions, and device unregistration. The existing Android repository remains the local integration boundary. Acknowledge, Resolve and status filters remain hidden pending a real contract. Read state is device-local.

Firebase-rendered background notifications can open the app without a local detail, and messages outside the callback may be absent from History. Data-only messages remain unsupported. Distinct FCM message IDs keep distinct events even when content matches. Without an upstream ID, repeat deliveries cannot reliably be identified. When SQLite fails, the fallback tray notification can have no cached detail. Cached data is available to the owning admin with a valid session, and a session that needs remote refresh can gate access while offline.

## Files changed

- `MainActivity.kt` for connectivity recovery.
- `NotificationDatabase.kt` for conflict classification.
- `NotificationCenterViewModel.kt` for observable list recovery.
- `NotificationScreens.kt` and `DashboardScreen.kt` for display changes.
- `NotificationListTest.kt` for JVM regressions.
- `docs/DAY6_IMPLEMENTATION_REPORT.md` and `docs/DAY6_TEST_CASES.md`.

## Day 7

Confirm the deployed admin sender and validate the exact payload and lifecycle behavior on an Android device. Confirm list, detail, severity and action contracts with the backend owner before wiring incident status. Prioritize an approved signed build, logout cleanup verification and a device matrix for permissions, offline recovery and accessibility.
