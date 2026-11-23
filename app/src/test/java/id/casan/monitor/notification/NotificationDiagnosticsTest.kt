package id.casan.monitor.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationDiagnosticsTest {
    private val id = "1234567890ab".repeat(5) + "cdef"
    private val receipt = NotificationReceipt(
        accountId = 7,
        receivedAt = 100,
        activityResumed = true,
        payload = PayloadResult.ACCEPTED,
        reference = id.take(12),
        cache = CacheResult.SAVED,
    )

    @Test
    fun receiptAndDetailAreIndependentAndScoped() {
        val diagnostics = NotificationDiagnostics()
        diagnostics.activate(7)
        diagnostics.opened(7, id)
        assertEquals(receipt.reference, diagnostics.state.value.opened?.reference)
        assertNull(diagnostics.state.value.receipt)
        diagnostics.activate(8)
        assertNull(diagnostics.state.value.opened)
        diagnostics.record(receipt)
        diagnostics.opened(8, id)
        assertNull(diagnostics.state.value.opened)
        diagnostics.opened(7, "raw-message-id")
        assertNull(diagnostics.state.value.opened)
        diagnostics.opened(7, id)
        assertEquals(receipt.reference, diagnostics.state.value.opened?.reference)
        assertEquals(receipt, diagnostics.state.value.receipt)
        diagnostics.record(receipt.copy(accountId = 8))
        assertNull(diagnostics.state.value.opened)
    }

    @Test
    fun signOutClearsObservationsAndRejectsLateDetailEvent() {
        val diagnostics = NotificationDiagnostics()
        diagnostics.record(receipt)
        diagnostics.clear()
        diagnostics.opened(7, id)
        assertEquals(NotificationDiagnosticState(), diagnostics.state.value)
    }

    @Test
    fun failedCacheWritesUseAlertsFallbackWithDistinctTargets() {
        val otherId = "f".repeat(64)
        val firstFallback = notificationTapTarget(7, id, cached = false)
        val secondFallback = notificationTapTarget(7, otherId, cached = false)
        assertNull(notificationDetailFor(firstFallback, 7))
        assertNull(notificationDetailFor(secondFallback, 7))
        assertEquals(id, firstFallback)
        assertEquals(otherId, secondFallback)
        assertEquals(id, notificationDetailFor(notificationTapTarget(7, id, cached = true), 7))
    }
}
