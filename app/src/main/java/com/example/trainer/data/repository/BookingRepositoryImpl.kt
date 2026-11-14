package com.example.trainer.data.repository

import com.example.trainer.data.mapper.toDomain
import com.example.trainer.data.remote.api.TrainerApi
import com.example.trainer.domain.model.*
import com.example.trainer.domain.repository.BookingRepository
import javax.inject.Inject

/** Remote implementation of the booking domain boundary. */
class BookingRepositoryImpl @Inject constructor(private val api: TrainerApi) : BookingRepository {
    override suspend fun book(trainerId: Int, startsAt: String) = api.book(trainerId, startsAt).toDomain()
    override suspend fun myBookings() = api.myBookings().map { it.toDomain() }
    override suspend fun cancel(id: Int) = api.cancel(id)
    override suspend fun slots(trainerId: Int, date: String) = api.slots(trainerId, date)
}

