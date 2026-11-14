package com.example.trainer.domain.repository

import com.example.trainer.domain.model.User

/** Authentication and verified server profile boundary. */
interface AuthRepository {
    suspend fun login(email: String, password: String): User
    suspend fun register(name: String, email: String, password: String): User
    suspend fun currentUser(): User?
    fun isSignedIn(): Boolean
    suspend fun logout()
}

