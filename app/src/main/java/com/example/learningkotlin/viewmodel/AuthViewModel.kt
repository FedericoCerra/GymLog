package com.example.learningkotlin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    var currentUser by mutableStateOf(auth.currentUser)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var authError by mutableStateOf<String?>(null)
        private set

    init {
        auth.addAuthStateListener {
            currentUser = it.currentUser
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun clearError() {
        authError = null
    }

    fun signIn(email: String, pass: String, onResult: (Boolean) -> Unit) {
        isLoading = true
        authError = null
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    onResult(true)
                } else {
                    authError = task.exception?.localizedMessage ?: "Sign in failed"
                    onResult(false)
                }
            }
    }

    fun signUp(email: String, username: String, pass: String, onResult: (Boolean) -> Unit) {
        isLoading = true
        authError = null
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            isLoading = false
                            if (profileTask.isSuccessful) {
                                onResult(true)
                            } else {
                                authError = profileTask.exception?.localizedMessage ?: "Profile update failed"
                                onResult(false)
                            }
                        }
                } else {
                    isLoading = false
                    authError = task.exception?.localizedMessage ?: "Sign up failed"
                    onResult(false)
                }
            }
    }
}
