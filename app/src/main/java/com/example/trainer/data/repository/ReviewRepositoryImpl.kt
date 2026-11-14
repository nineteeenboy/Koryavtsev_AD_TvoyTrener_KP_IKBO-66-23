package com.example.trainer.data.repository

import com.example.trainer.data.mapper.toDomain
import com.example.trainer.data.remote.api.TrainerApi
import com.example.trainer.domain.model.*
import com.example.trainer.domain.repository.ReviewRepository
import javax.inject.Inject

/** Remote implementation of the review domain boundary. */
class ReviewRepositoryImpl @Inject constructor(private val api: TrainerApi) : ReviewRepository {
    override suspend fun leaveReview(trainerId: Int, rating: Int, comment: String) = api.review(trainerId, rating, comment).toDomain()
    override suspend fun getReviews(trainerId: Int) = api.reviews(trainerId).map { it.toDomain() }
    override suspend fun myReviews() = api.myReviews().map { it.toDomain() }
}

