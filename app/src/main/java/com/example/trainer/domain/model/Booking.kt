package com.example.trainer.domain.model

/** Immutable booking domain entity, independent of HTTP and Android UI. */
data class Booking(val id: Int, val trainerId: Int, val trainerName: String,
    val startsAt: String, val status: String, val price: Double)

