package com.example.trainer.domain.usecase

import com.example.trainer.domain.model.*
import com.example.trainer.domain.repository.ReviewRepository

/** Leave Review application operation. */
class LeaveReviewUseCase(private val repository: ReviewRepository) {
    suspend operator fun invoke(id: Int, rating: Int, comment: String): Review {
        require(id > 0) { "Некорректный тренер" }
        require(rating in 1..5) { "Оценка должна быть от 1 до 5" }
        require(comment.trim().length in 3..2000) { "Отзыв должен содержать 3–2000 символов" }
        return repository.leaveReview(id, rating, comment.trim())
    }
}

