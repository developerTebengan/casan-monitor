package id.casan.monitor.data.remote

import com.google.gson.Gson
import id.casan.monitor.domain.model.MonitorAccount
import org.junit.Assert.*
import org.junit.Test

class AuthDtosTest {
    private val gson = Gson()
    private val account = MonitorAccount(42, "Operator", "ops@example.test")

    @Test
    fun decodesAdminLoginEnvelopeAndSecondsBasedExpiry() {
        val envelope = decode(
            """{"status":"success","data":{"token":"access","refresh_token":"refresh","expires_in":60,
                "admin":{"ID":42,"Name":"Operator","Email":"ops@example.test"}}}""",
        )

        val session = requireNotNull(envelope.toSession(now = 1000))

        assertEquals(account, session.account)
        assertEquals("access", session.accessToken)
        assertEquals("refresh", session.refreshToken)
        assertFalse(session.isExpired(60_999))
        assertTrue(session.isExpired(61_000))
    }

    @Test
    fun refreshKeepsAccountAndReplacesBothCredentials() {
        val envelope = decode(
            """{"status":"success","data":{"token":"replacement","refresh_token":"rotated","expires_in":5}}""",
        )
        val session = requireNotNull(envelope.toSession(account, now = 0))
        assertEquals(account, session.account)
        assertEquals("replacement", session.accessToken)
        assertEquals("rotated", session.refreshToken)
        assertEquals(5000L, session.expiresAt)
    }

    @Test
    fun rejectsFailedOrMalformedResponses() {
        assertNull(decode("""{"status":"error"}""").toSession(account))
        assertNull(
            decode("""{"status":"success","data":{"token":"a","refresh_token":"b","expires_in":0}}""")
                .toSession(account),
        )
        assertNull(
            decode("""{"status":"success","data":{"token":"a","expires_in":60}}""")
                .toSession(account),
        )
    }

    @Test
    fun serializesExactAdminLoginAndRefreshRequestFields() {
        assertEquals(
            """{"email":"ops@example.test","password":"example"}""",
            gson.toJson(LoginRequest(account.email, "example")),
        )
        assertEquals("""{"refresh_token":"refresh"}""", gson.toJson(RefreshRequest("refresh")))
    }

    private fun decode(json: String): AuthEnvelope = gson.fromJson(json, AuthEnvelope::class.java)
}
