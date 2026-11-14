package com.example.trainer.domain.usecase

import com.example.trainer.domain.model.*
import com.example.trainer.domain.repository.TrainerRepository

/** Get Trainers application operation. */
class GetTrainersUseCase(private val repository: TrainerRepository) {
    suspend operator fun invoke(filter: TrainerFilter = TrainerFilter()): List<Trainer> {
        require(filter.maxPrice == null || (filter.maxPrice.isFinite() && filter.maxPrice >= 0)) { "Некорректная цена" }
        require(filter.minRating == null || (filter.minRating.isFinite() && filter.minRating in 0.0..5.0)) { "Некорректный рейтинг" }
        return repository.getTrainers(filter)
    }
}

