package com.example.trainer.domain.usecase

import com.example.trainer.domain.model.*
import com.example.trainer.domain.repository.TrainerRepository

/** Get Trainer By Id application operation. */
class GetTrainerByIdUseCase(private val repository: TrainerRepository) {
    suspend operator fun invoke(id: Int): Trainer {
        require(id > 0) { "Некорректный тренер" }
        return repository.getTrainerById(id)
    }
}

