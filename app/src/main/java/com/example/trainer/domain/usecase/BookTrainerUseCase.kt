package com.example.trainer.domain.usecase

import com.example.trainer.domain.model.*
import com.example.trainer.domain.repository.BookingRepository

/** Book Trainer application operation. */
class BookTrainerUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(id: Int, startsAt: String): Booking {
        require(id > 0) { "Некорректный тренер" }
        require(java.time.Instant.parse(startsAt) > java.time.Instant.now()) { "Выберите будущее время" }
        return repository.book(id, startsAt)
    }
}

