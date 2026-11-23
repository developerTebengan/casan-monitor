package id.casan.monitor.data.remote

import com.google.gson.Gson
import id.casan.monitor.domain.model.MonitorAccount
import org.junit.Assert.*
import org.junit.Test

class AuthDtosTest {
    private val gson = Gson()
    private val account = MonitorAccount(42, "Operator", "ops@example.test", "")

    @Test
    fun decodesAdminLoginEnvelopeAndExpiry() {
        val response = decode(
            """{"status":"success","data":{"token":"access","refresh_token":"refresh","expires_in":60,"admin":{"ID":42,"Name":"Operator","Email":"ops@example.test"}}}""",
        )
        val session = requireNotNull(response.toSession(now = 1000))
        assertEquals(account, session.account)
        assertEquals("access", session.accessToken)
        assertEquals("refresh", session.refreshToken)
        assertFalse(session.isExpired(60_999))
        assertTrue(session.isExpired(61_000))
    }

    @Test
    fun loginRequiresAnAdminIdentity() {
        assertNull(decode("""{"status":"success","data":{"token":"a","refresh_token":"b","expires_in":60}}""").toSession())
        assertNull(
            decode(
                """{"status":"success","data":{"token":"a","refresh_token":"b","expires_in":60,"admin":{"ID":0,"Email":"ops@example.test"}}}""",
            ).toSession(),
        )
    }

    @Test
    fun refreshPreservesAccountAndRotatesCredentials() {
        val response = decode(
            """{"status":"success","data":{"token":"replacement","refresh_token":"rotated","expires_in":5}}""",
        )
        val session = requireNotNull(response.toSession(account, now = 0))
        assertEquals(account, session.account)
        assertEquals("replacement", session.accessToken)
        assertEquals("rotated", session.refreshToken)
        assertEquals(5000L, session.expiresAt)
    }

    @Test
    fun rejectsMalformedTokenPairs() {
        assertNull(decode("""{"status":"error"}""").toSession(account))
        assertNull(
            decode("""{"status":"success","data":{"token":"a","refresh_token":"b","expires_in":0}}""")
                .toSession(account),
        )
    }

    @Test
    fun serializesAdminLoginAndFirebaseContracts() {
        assertEquals("""{"email":"ops@example.test","password":"test-password"}""", gson.toJson(LoginRequest(account.email, "test-password")))
        assertEquals("""{"refresh_token":"refresh"}""", gson.toJson(RefreshRequest("refresh")))
        assertEquals("""{"token":"test-token","platform":"android"}""", gson.toJson(FirebaseTokenRequest("test-token")))
    }

    private fun decode(json: String): AuthEnvelope = gson.fromJson(json, AuthEnvelope::class.java)
}
