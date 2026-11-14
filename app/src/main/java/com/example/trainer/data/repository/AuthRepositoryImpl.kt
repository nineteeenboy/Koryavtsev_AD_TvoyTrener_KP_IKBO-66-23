package com.example.trainer.data.repository

import com.example.trainer.data.auth.FirebaseAuthDataSource
import com.example.trainer.data.mapper.toDomain
import com.example.trainer.data.remote.api.TrainerApi
import com.example.trainer.domain.repository.AuthRepository
import javax.inject.Inject

/** Completes Firebase authentication before synchronizing the server profile. */
class AuthRepositoryImpl @Inject constructor(
    private val firebase: FirebaseAuthDataSource, private val api: TrainerApi,
) : AuthRepository {
    override suspend fun login(email: String, password: String) =
        firebase.login(email, password).let { api.login().toDomain() }
    override suspend fun register(name: String, email: String, password: String) =
        firebase.register(name, email, password).let { api.register(name).toDomain() }
    override suspend fun currentUser() = if (isSignedIn()) api.profile().toDomain() else null
    override fun isSignedIn() = firebase.currentUser() != null
    override suspend fun logout() = firebase.logout()
}

