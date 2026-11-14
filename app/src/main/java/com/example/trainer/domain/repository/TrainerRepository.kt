package com.example.trainer.domain.repository

import com.example.trainer.domain.model.*

/** Trainer catalog boundary. */
interface TrainerRepository {
    suspend fun getTrainers(filter: TrainerFilter): List<Trainer>
    suspend fun getTrainerById(id: Int): Trainer
}

