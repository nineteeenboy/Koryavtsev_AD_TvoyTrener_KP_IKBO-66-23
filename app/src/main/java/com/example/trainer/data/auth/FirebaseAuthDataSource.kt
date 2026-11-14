package com.example.trainer.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Firebase owns password verification and session persistence. */
@Singleton
class FirebaseAuthDataSource @Inject constructor(private val auth: FirebaseAuth) {
    suspend fun register(name: String, email: String, password: String) {
        val user = auth.createUserWithEmailAndPassword(email, password).await().user
            ?: error("Не удалось создать аккаунт")
        user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()
    }
    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }
    fun logout() = auth.signOut()
    fun currentUser() = auth.currentUser
    suspend fun getToken(forceRefresh: Boolean = false): String? =
        auth.currentUser?.getIdToken(forceRefresh)?.await()?.token
}

