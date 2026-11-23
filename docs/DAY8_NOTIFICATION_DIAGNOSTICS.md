# Day 8 notification diagnostics

In a debug build, sign in and open **Settings → Notification diagnostics**. The panel is account-scoped and held in memory. It clears on logout or process exit. It contains no token, raw FCM identifier, account identifier on screen, title, body or credential.

| Observation | Interpretation |
|---|---|
| Existing registration status | Waiting, registering, registered, offline, Firebase unavailable, or failed. Registered may come from a previously confirmed matching association |
| FCM message received | App callback timestamp, plus whether MainActivity was resumed at receipt. This is client time |
| Payload | Accepted, unsupported, customer event, or invalid. Observations require an eligible registered admin session |
| Reference | First 12 hex characters of the same local SHA-256 notification identifier used for storage |
| Cache | Saved, duplicate, or failed. Duplicates stop before posting |
| Channel and tray | Info channel and local posting result. A successful notify call records a post timestamp. OS presentation still depends on the device |
| Detail opened | A saved notification detail loaded for the account, either from the list or a tap. Recorded separately from receipt, with a safe reference |

When SQLite fails, the tray can still be posted and the tap opens Alerts. For a cached row, the existing account-scoped target remains in place. Repeat callback delivery of the same FCM message ID uses the same database key. Separate Firebase messages with different IDs can represent distinct alerts even when the text matches. The backend does not provide a stable incident event ID.

Firebase may render background notification or mixed payloads without calling the app's receiver. The debug panel cannot observe these system-rendered receipts or infer a server send time, a backend acceptance result, lock-screen display, or delivery after force-stop. Offline delivery can be delayed or missed. Compare sender acceptance, the tray, the diagnostic snapshot and cached history as separate evidence. The user's earlier Firebase Console success was prior evidence. No new Day 8 backend, lock-screen, foreground or background delivery was executed.
