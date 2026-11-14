package com.example.trainer.data.repository

import com.example.trainer.data.mapper.toDomain
import com.example.trainer.data.remote.api.TrainerApi
import com.example.trainer.domain.model.*
import com.example.trainer.domain.repository.TrainerRepository
import javax.inject.Inject

/** Remote implementation of the trainer domain boundary. */
class TrainerRepositoryImpl @Inject constructor(private val api: TrainerApi) : TrainerRepository {
    override suspend fun getTrainers(filter: TrainerFilter) = api.trainers(filter).map { it.toDomain() }
    override suspend fun getTrainerById(id: Int) = api.trainer(id).toDomain()
}

