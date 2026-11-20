package id.casan.monitor.data.remote

import com.google.gson.Gson
import id.casan.monitor.domain.model.MonitorAccount
import org.junit.Assert.*
import org.junit.Test

class AuthDtosTest {
    private val gson = Gson()
    private val account = MonitorAccount(42, "Operator", "ops@example.test", "+6281234567890")

    @Test
    fun decodesUserLoginEnvelopeAndExpiry() {
        val response = decode(
            """{"status":"success","data":{"token":"access","refresh_token":"refresh","expires_in":60}}""",
        )
        val session = requireNotNull(response.toSession(account, now = 1000))
        assertEquals(account, session.account)
        assertEquals("access", session.accessToken)
        assertEquals("refresh", session.refreshToken)
        assertFalse(session.isExpired(60_999))
        assertTrue(session.isExpired(61_000))
    }

    @Test
    fun mapsOnlyRequiredFieldsFromAuthenticatedProfile() {
        val response = gson.fromJson(
            """{"status":"success","data":{"ID":42,"Name":"Operator","Email":"ops@example.test",
                "Phone":"+6281234567890","NIK":"ignored"}}""",
            UserEnvelope::class.java,
        )
        assertEquals(account, response.toAccount())
        assertNull(UserEnvelope("success", UserResponse(0, null, null, null)).toAccount())
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
    fun serializesTheExistingOtpAndFirebaseContracts() {
        assertEquals("""{"phone_number":"+6281234567890","channel":2}""", gson.toJson(SendOtpRequest(account.phone, 2)))
        assertEquals(
            """{"phone_number":"+6281234567890","code":"0123","channel":1}""",
            gson.toJson(LoginRequest(account.phone, "0123", 1)),
        )
        assertEquals("""{"refresh_token":"refresh"}""", gson.toJson(RefreshRequest("refresh")))
        assertEquals("""{"token":"test-token","platform":"android"}""", gson.toJson(FirebaseTokenRequest("test-token")))
    }

    private fun decode(json: String): AuthEnvelope = gson.fromJson(json, AuthEnvelope::class.java)
}
