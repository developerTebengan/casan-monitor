package id.casan.monitor.data.remote

import com.google.gson.annotations.SerializedName
import id.casan.monitor.domain.model.MonitorAccount
import id.casan.monitor.domain.model.MonitorSession

class LoginRequest(
    @SerializedName("phone_number") val phone: String,
    val code: String,
    val channel: Int,
)

class SendOtpRequest(@SerializedName("phone_number") val phone: String, val channel: Int)
class RefreshRequest(@SerializedName("refresh_token") val token: String)
class StatusEnvelope(val status: String?)
class FirebaseTokenRequest(val token: String, val platform: String = "android")

class UserEnvelope(val status: String?, val data: UserResponse?)
class UserResponse(
    @SerializedName("ID") val id: Long,
    @SerializedName("Name") val name: String?,
    @SerializedName("Email") val email: String?,
    @SerializedName("Phone") val phone: String?,
)

internal fun UserEnvelope.toAccount(): MonitorAccount? {
    val profile = data ?: return null
    if (status != "success" || profile.id <= 0) return null
    return MonitorAccount(profile.id, profile.name.orEmpty(), profile.email.orEmpty(), profile.phone.orEmpty())
}

class AuthEnvelope(val status: String?, val data: TokenResponse?)
class TokenResponse(
    val token: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("expires_in") val expiresIn: Long,
)

internal fun AuthEnvelope.toSession(
    account: MonitorAccount,
    now: Long = System.currentTimeMillis(),
): MonitorSession? {
    val pair = data ?: return null
    if (status != "success" || pair.expiresIn !in 1L..((Long.MAX_VALUE - now) / 1000)) return null
    val access = pair.token?.takeIf(String::isNotBlank) ?: return null
    val refresh = pair.refreshToken?.takeIf(String::isNotBlank) ?: return null
    return MonitorSession(account, access, refresh, now + pair.expiresIn * 1000)
}
