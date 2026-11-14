package com.example.trainer.domain.usecase

import com.example.trainer.domain.model.*
import com.example.trainer.domain.repository.ReviewRepository

/** Get My Reviews application operation. */
class GetMyReviewsUseCase(private val repository: ReviewRepository) {
    suspend operator fun invoke(): List<Review> = repository.myReviews()
}

