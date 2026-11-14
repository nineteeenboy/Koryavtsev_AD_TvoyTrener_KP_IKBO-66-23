package com.example.trainer.domain.usecase

import com.example.trainer.domain.model.*
import com.example.trainer.domain.repository.BookingRepository

/** Get Available Slots application operation. */
class GetAvailableSlotsUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(id: Int, date: String): List<String> {
        require(id > 0) { "Некорректный тренер" }
        java.time.LocalDate.parse(date)
        return repository.slots(id, date)
    }
}

