package id.casan.monitor.data.remote

import com.google.gson.annotations.SerializedName
import id.casan.monitor.domain.model.MonitorAccount
import id.casan.monitor.domain.model.MonitorSession

class LoginRequest(val email: String, val password: String)
class RefreshRequest(@SerializedName("refresh_token") val token: String)
class StatusEnvelope(val status: String?)
class FirebaseTokenRequest(val token: String, val platform: String = "android")

class AuthEnvelope(val status: String?, val data: TokenResponse?)
class TokenResponse(
    val token: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("expires_in") val expiresIn: Long,
    val admin: AdminResponse?,
)

class AdminResponse(
    @SerializedName("ID") val id: Long,
    @SerializedName("Name") val name: String?,
    @SerializedName("Email") val email: String?,
)

internal fun AuthEnvelope.toSession(
    existingAccount: MonitorAccount? = null,
    now: Long = System.currentTimeMillis(),
): MonitorSession? {
    val pair = data ?: return null
    if (status != "success" || pair.expiresIn !in 1L..((Long.MAX_VALUE - now) / 1000)) return null
    val access = pair.token?.takeIf(String::isNotBlank) ?: return null
    val refresh = pair.refreshToken?.takeIf(String::isNotBlank) ?: return null
    val account = existingAccount ?: pair.admin?.let { admin ->
        if (admin.id <= 0) return null
        val email = admin.email?.takeIf(String::isNotBlank) ?: return null
        MonitorAccount(admin.id, admin.name.orEmpty(), email, "")
    } ?: return null
    return MonitorSession(account, access, refresh, now + pair.expiresIn * 1000)
}
