package id.casan.monitor.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("api/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<StatusEnvelope>

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthEnvelope>

    @GET("api/users/me")
    suspend fun profile(@Header("Authorization") authorization: String): Response<UserEnvelope>

    @POST("api/refresh-token")
    suspend fun refresh(@Body request: RefreshRequest): Response<AuthEnvelope>

    @POST("api/logout")
    suspend fun logout(@Body request: RefreshRequest): Response<Unit>

    @POST("api/firebase/token")
    suspend fun registerDevice(
        @Header("Authorization") authorization: String,
        @Body request: FirebaseTokenRequest,
    ): Response<StatusEnvelope>
}
