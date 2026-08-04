package com.example.data.auth

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthRepository {
    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null
    }
    
    private val _currentUser = MutableStateFlow(auth?.currentUser)
    
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()
    val isSignedIn: Flow<Boolean> = _currentUser.map { it != null }

    init {
        auth?.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            if (auth == null) throw Exception("Firebase is not configured. Please add google-services.json to the app.")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(Exception(mapAuthException(e)))
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            if (auth == null) throw Exception("Firebase is not configured. Please add google-services.json to the app.")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(Exception(mapAuthException(e)))
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            if (auth == null) throw Exception("Firebase is not configured. Please add google-services.json to the app.")
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(mapAuthException(e)))
        }
    }

    fun startPhoneVerification(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (auth == null) {
            onError("Firebase is not configured. Please add google-services.json to the app.")
            return
        }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Handled manually via verifyPhoneCode
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    onError(mapAuthException(e))
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    onCodeSent(verificationId)
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyPhoneCode(verificationId: String, code: String): Result<FirebaseUser> {
        return try {
            if (auth == null) throw Exception("Firebase is not configured. Please add google-services.json to the app.")
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val result = auth.signInWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(Exception(mapAuthException(e)))
        }
    }

    suspend fun signOut() {
        auth?.signOut()
    }

    private fun mapAuthException(e: Exception): String {
        return when (e) {
            is FirebaseAuthInvalidCredentialsException -> {
                if (e.errorCode == "ERROR_INVALID_EMAIL") "The email address is badly formatted."
                else "Invalid credentials. Please check your email and password."
            }
            is FirebaseAuthInvalidUserException -> "No account found with this email."
            is FirebaseAuthUserCollisionException -> "An account already exists with this email."
            is FirebaseAuthWeakPasswordException -> "Password should be at least 6 characters."
            is FirebaseNetworkException -> "A network error occurred. Please check your connection."
            is FirebaseTooManyRequestsException -> "Too many attempts. Please try again later."
            else -> "An error occurred: ${e.message}"
        }
    }
}
