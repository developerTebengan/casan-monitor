package id.casan.monitor.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/admin-login")
    suspend fun login(@Body request: LoginRequest): Response<AuthEnvelope>

    @POST("api/refresh-token")
    suspend fun refresh(@Body request: RefreshRequest): Response<AuthEnvelope>

    @POST("api/logout")
    suspend fun logout(@Body request: RefreshRequest): Response<Unit>
}
