package com.example.trainer.server.dto

import kotlinx.serialization.Serializable

/** Verified user profile, without passwords or tokens. */
@Serializable
data class UserDto(val id: String, val email: String, val name: String,
    val role: String = "CLIENT", val trainerId: Int? = null)
/** Public trainer card and aggregated review score. */
@Serializable
data class TrainerDto(
    val id: Int, val name: String, val specialization: String, val photoUrl: String,
    val description: String, val experienceYears: Int, val price: Double,
    val rating: Double, val reviewCount: Int, val isFeatured: Boolean = false,
)
/** Training reservation; startsAt is an ISO-8601 UTC instant. */
@Serializable
data class BookingDto(
    val id: Int, val trainerId: Int, val trainerName: String,
    val startsAt: String, val status: String, val price: Double,
)
/** Published review. */
@Serializable
data class ReviewDto(
    val id: Int, val trainerId: Int, val authorName: String, val rating: Int,
    val comment: String, val createdAt: String,
)
/** Registration synchronizes a Firebase account with the local profile. */
@Serializable
data class RegisterRequest(val name: String)
/** New one-hour reservation. */
@Serializable
data class BookingRequest(val startsAt: String)
/** One review per client and trainer. */
@Serializable
data class ReviewRequest(val rating: Int, val comment: String)
/** Consistent JSON error envelope. */
@Serializable
data class ErrorDto(val code: String, val message: String)
/** Available hourly UTC instants on the requested gym-local date. */
@Serializable
data class SlotsDto(val date: String, val zoneId: String, val startsAt: List<String>)

