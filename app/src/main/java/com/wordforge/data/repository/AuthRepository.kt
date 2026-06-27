package com.wordforge.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.wordforge.data.remote.AuthApi
import com.wordforge.data.remote.FirebaseAuthRequest
import com.wordforge.util.Result
import com.wordforge.util.TokenManager
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    suspend fun signInWithFirebase(credential: PhoneAuthCredential): Result<Unit> = try {
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        val idToken = authResult.user
            ?.getIdToken(false)
            ?.await()
            ?.token
            ?: return Result.Error("Could not retrieve Firebase ID token")
        val response = authApi.firebaseAuth(FirebaseAuthRequest(idToken))
        tokenManager.saveToken(response.token)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Authentication failed")
    }

    fun signOut() {
        firebaseAuth.signOut()
        tokenManager.clearToken()
    }
}
