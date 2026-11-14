package com.example.trainer.data.mapper

import com.example.trainer.data.remote.dto.*
import com.example.trainer.domain.model.*
import com.example.trainer.BuildConfig
import java.net.URI

fun UserDto.toDomain() = User(id, email, name, role, trainerId)
fun TrainerDto.toDomain() = Trainer(id, name, specialization,
    URI(BuildConfig.API_BASE_URL).resolve(photoUrl).toString(), description, experienceYears, price, rating, reviewCount, isFeatured)
fun BookingDto.toDomain() = Booking(id, trainerId, trainerName, startsAt, status, price)
fun ReviewDto.toDomain() = Review(id, trainerId, authorName, rating, comment, createdAt)

fun User.toDto() = UserDto(id, email, name, role, trainerId)
fun Trainer.toDto() = TrainerDto(id, name, specialization, photoUrl, description, experienceYears, price, rating, reviewCount, isFeatured)
fun Booking.toDto() = BookingDto(id, trainerId, trainerName, startsAt, status, price)
fun Review.toDto() = ReviewDto(id, trainerId, authorName, rating, comment, createdAt)

