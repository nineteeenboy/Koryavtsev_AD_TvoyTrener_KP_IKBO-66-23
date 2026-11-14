package com.example.trainer.domain.usecase

import com.example.trainer.domain.model.*
import com.example.trainer.domain.repository.AuthRepository

/** Register application operation. */
class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(name: String, email: String, password: String): User {
        require(name.trim().length in 2..100) { "Имя должно содержать 2–100 символов" }
        validateCredentials(email, password)
        return repository.register(name.trim(), email.trim(), password)
    }
}

