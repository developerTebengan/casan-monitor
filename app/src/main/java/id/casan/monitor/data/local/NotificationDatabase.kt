package id.casan.monitor.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import id.casan.monitor.BuildConfig
import id.casan.monitor.domain.model.MonitorNotification
import id.casan.monitor.domain.model.NOTIFICATION_WINDOW_SIZE
import id.casan.monitor.domain.model.NotificationCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationDatabase @Inject constructor(@ApplicationContext context: Context) :
    SQLiteOpenHelper(context, "monitor_notifications.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE notifications (
                owner TEXT NOT NULL,
                notification_id TEXT NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                category TEXT,
                received_at INTEGER NOT NULL,
                is_read INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (owner, notification_id)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX notifications_order ON notifications(owner, received_at DESC, notification_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        error("No migration from notification schema $oldVersion to $newVersion")
    }

    fun list(accountId: Long): List<MonitorNotification> =
        readableDatabase.rawQuery(
            """
            SELECT notification_id, title, message, category, received_at, is_read
            FROM notifications WHERE owner = ?
            ORDER BY received_at DESC, notification_id
            LIMIT $NOTIFICATION_WINDOW_SIZE
            """.trimIndent(),
            arrayOf(owner(accountId)),
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.notification())
            }
        }

    fun detail(accountId: Long, notificationId: String): MonitorNotification? =
        readableDatabase.rawQuery(
            """
            SELECT notification_id, title, message, category, received_at, is_read
            FROM notifications WHERE owner = ? AND notification_id = ?
            """.trimIndent(),
            arrayOf(owner(accountId), notificationId),
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.notification() else null
        }

    private fun Cursor.notification() = MonitorNotification(
        getString(0),
        getString(1),
        getString(2),
        NotificationCategory.entries.firstOrNull { it.name == getString(3) },
        getLong(4),
        getInt(5) != 0,
    )

    fun insert(accountId: Long, notification: MonitorNotification): Boolean {
        val values = ContentValues().apply {
            put("owner", owner(accountId))
            put("notification_id", notification.id)
            put("title", notification.title)
            put("message", notification.message)
            put("category", notification.category?.name)
            put("received_at", notification.receivedAt)
        }
        return try {
            writableDatabase.insertOrThrow("notifications", null, values)
            true
        } catch (_: SQLiteConstraintException) {
            // A redelivery preserves the original timestamp and read state.
            false
        }
    }

    fun markRead(accountId: Long, notificationId: String): Boolean =
        writableDatabase.update(
            "notifications",
            ContentValues().apply { put("is_read", 1) },
            "owner = ? AND notification_id = ? AND is_read = 0",
            arrayOf(owner(accountId), notificationId),
        ) > 0

    private fun owner(accountId: Long) = "${BuildConfig.BACKEND_URL}|admin|$accountId"
}
