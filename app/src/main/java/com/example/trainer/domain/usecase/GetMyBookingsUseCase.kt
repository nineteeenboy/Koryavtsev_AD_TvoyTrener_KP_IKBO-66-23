package com.example.trainer.domain.usecase

import com.example.trainer.domain.model.*
import com.example.trainer.domain.repository.BookingRepository

/** Get My Bookings application operation. */
class GetMyBookingsUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(): List<Booking> = repository.myBookings()
}

