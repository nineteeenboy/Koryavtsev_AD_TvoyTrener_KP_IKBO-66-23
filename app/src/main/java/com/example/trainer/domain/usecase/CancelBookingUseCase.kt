package com.example.trainer.domain.usecase

import com.example.trainer.domain.model.*
import com.example.trainer.domain.repository.BookingRepository

/** Cancel Booking application operation. */
class CancelBookingUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(id: Int) {
        require(id > 0) { "Некорректная запись" }
        repository.cancel(id)
    }
}

