package com.wordforge.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class FirebaseAuthRequest(val idToken: String)
data class AuthResponse(val token: String, val userId: Long)

interface AuthApi {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/firebase")
    suspend fun firebaseAuth(@Body request: FirebaseAuthRequest): AuthResponse
}
