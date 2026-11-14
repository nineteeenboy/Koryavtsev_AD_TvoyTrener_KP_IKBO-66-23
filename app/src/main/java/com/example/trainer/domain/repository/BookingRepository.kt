package com.example.trainer.domain.repository

import com.example.trainer.domain.model.Booking

/** Reservations and available UTC instants. */
interface BookingRepository {
    suspend fun book(trainerId: Int, startsAt: String): Booking
    suspend fun myBookings(): List<Booking>
    suspend fun cancel(id: Int)
    suspend fun slots(trainerId: Int, date: String): List<String>
}

