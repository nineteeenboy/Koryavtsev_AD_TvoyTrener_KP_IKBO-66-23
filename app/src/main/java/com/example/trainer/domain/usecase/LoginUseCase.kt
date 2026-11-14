package com.example.trainer.domain.usecase

import com.example.trainer.domain.model.*
import com.example.trainer.domain.repository.AuthRepository

/** Login application operation. */
class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): User {
        validateCredentials(email, password)
        return repository.login(email.trim(), password)
    }
}

