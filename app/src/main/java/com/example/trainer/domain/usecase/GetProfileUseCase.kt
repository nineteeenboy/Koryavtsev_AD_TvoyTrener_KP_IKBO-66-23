package com.example.trainer.domain.usecase

import com.example.trainer.domain.model.*
import com.example.trainer.domain.repository.AuthRepository

/** Get Profile application operation. */
class GetProfileUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): User? = repository.currentUser()
}

