package com.example.trainer.data.remote.dto

import kotlinx.serialization.Serializable

/** JSON user profile. */
@Serializable data class UserDto(val id: String, val email: String, val name: String,
    val role: String = "CLIENT", val trainerId: Int? = null)
/** JSON trainer card. */
@Serializable data class TrainerDto(
    val id: Int, val name: String, val specialization: String, val photoUrl: String,
    val description: String, val experienceYears: Int, val price: Double,
    val rating: Double, val reviewCount: Int, val isFeatured: Boolean = false,
)
/** JSON reservation. */
@Serializable data class BookingDto(
    val id: Int, val trainerId: Int, val trainerName: String,
    val startsAt: String, val status: String, val price: Double,
)
/** JSON review. */
@Serializable data class ReviewDto(
    val id: Int, val trainerId: Int, val authorName: String, val rating: Int,
    val comment: String, val createdAt: String,
)
/** Profile synchronization body. */
@Serializable data class RegisterRequest(val name: String)
/** Reservation request. */
@Serializable data class BookingRequest(val startsAt: String)
/** Review publication body. */
@Serializable data class ReviewRequest(val rating: Int, val comment: String)
/** Safe server error. */
@Serializable data class ErrorDto(val code: String, val message: String)
/** Available hourly instants in gym timezone. */
@Serializable data class SlotsDto(val date: String, val zoneId: String, val startsAt: List<String>)

