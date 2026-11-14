package com.example.trainer.domain.repository

import com.example.trainer.domain.model.Review

/** Trainer reviews and personal review history. */
interface ReviewRepository {
    suspend fun leaveReview(trainerId: Int, rating: Int, comment: String): Review
    suspend fun getReviews(trainerId: Int): List<Review>
    suspend fun myReviews(): List<Review>
}

