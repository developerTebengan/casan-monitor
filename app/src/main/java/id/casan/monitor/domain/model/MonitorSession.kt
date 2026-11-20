package id.casan.monitor.domain.model

data class MonitorAccount(val id: Long, val name: String, val email: String)

class MonitorSession(
    val account: MonitorAccount,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
) {
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean = now >= expiresAt
}
