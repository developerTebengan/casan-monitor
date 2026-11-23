package id.casan.monitor.notification

import id.casan.monitor.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

enum class PayloadResult { ACCEPTED, UNSUPPORTED, CUSTOMER_EVENT, INVALID }
enum class CacheResult { SAVED, DUPLICATE, FAILED }
enum class TrayResult { POSTED, APP_DISABLED, CHANNEL_DISABLED, PERMISSION_CHANGED }

data class NotificationReceipt(
    val accountId: Long,
    val receivedAt: Long,
    val activityResumed: Boolean,
    val payload: PayloadResult,
    val reference: String? = null,
    val cache: CacheResult? = null,
    val channel: String? = null,
    val tray: TrayResult? = null,
    val postedAt: Long? = null,
)

data class OpenedNotification(val reference: String, val openedAt: Long)

data class NotificationDiagnosticState(
    val accountId: Long? = null,
    val receipt: NotificationReceipt? = null,
    val opened: OpenedNotification? = null,
)

@Singleton
class NotificationDiagnostics @Inject constructor() {
    private val mutableState = MutableStateFlow(NotificationDiagnosticState())
    val state = mutableState.asStateFlow()

    @Volatile
    var activityResumed = false

    fun activate(accountId: Long) {
        if (!BuildConfig.DEBUG) return
        mutableState.update { if (it.accountId == accountId) it else NotificationDiagnosticState(accountId = accountId) }
    }

    fun record(receipt: NotificationReceipt) {
        if (!BuildConfig.DEBUG) return
        mutableState.update { previous ->
            NotificationDiagnosticState(
                accountId = receipt.accountId,
                receipt = receipt,
                opened = previous.opened.takeIf { previous.accountId == receipt.accountId },
            )
        }
    }

    fun opened(accountId: Long, id: String) {
        if (!BuildConfig.DEBUG || !isNotificationId(id)) return
        val event = OpenedNotification(id.take(12), System.currentTimeMillis())
        mutableState.update {
            if (it.accountId == accountId) it.copy(opened = event) else it
        }
    }

    fun clear() {
        if (BuildConfig.DEBUG) mutableState.value = NotificationDiagnosticState()
    }
}
