package com.example.trainer.domain.usecase

import com.example.trainer.domain.model.*
import com.example.trainer.domain.repository.ReviewRepository

/** Get Reviews application operation. */
class GetReviewsUseCase(private val repository: ReviewRepository) {
    suspend operator fun invoke(id: Int): List<Review> {
        require(id > 0) { "Некорректный тренер" }
        return repository.getReviews(id)
    }
}

