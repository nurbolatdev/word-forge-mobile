package com.wordforge.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.wordforge.data.repository.AuthRepository
import com.wordforge.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val authRepository: AuthRepository
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object CodeSent : UiState()
        object Authenticated : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // stored in-memory only, never in URL params
    private var verificationId: String? = null

    fun sendOtp(phoneNumber: String, activity: Activity) {
        _uiState.value = UiState.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signIn(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.value = UiState.Error(e.message ?: "OTP request failed")
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = id
                _uiState.value = UiState.CodeSent
            }
        }

        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
        )
    }

    fun verifyCode(code: String) {
        val id = verificationId ?: run {
            _uiState.value = UiState.Error("Session expired — request a new code")
            return
        }
        signIn(PhoneAuthProvider.getCredential(id, code))
    }

    fun clearError() {
        if (_uiState.value is UiState.Error) _uiState.value = UiState.Idle
    }

    private fun signIn(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = authRepository.signInWithFirebase(credential)) {
                is Result.Success -> _uiState.value = UiState.Authenticated
                is Result.Error -> _uiState.value = UiState.Error(result.message)
                else -> Unit
            }
        }
    }
}
