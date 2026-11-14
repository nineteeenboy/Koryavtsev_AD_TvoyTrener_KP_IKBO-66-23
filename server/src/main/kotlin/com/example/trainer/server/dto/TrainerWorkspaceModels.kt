package com.example.trainer.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrainerApplicationRequest(
    val email: String, val phone: String, val experience: String, val education: String,
    val certificates: String = "", val specialization: String = "Персональные тренировки",
    val experienceYears: Int = 0, val price: Double = 0.0,
)

@Serializable
data class TrainerApplicationDto(
    val id: Int, val userId: String, val name: String, val email: String, val phone: String,
    val specialization: String, val experienceYears: Int, val price: Double,
    val experience: String, val education: String, val certificates: String,
    val status: String, val submittedAt: String, val reviewedAt: String? = null,
    val adminComment: String = "",
)

@Serializable
data class ApplicationDecisionRequest(val approve: Boolean, val comment: String = "")

@Serializable
data class TrainingSessionDto(
    val id: Int, val clientName: String, val startsAt: String, val status: String,
    val price: Double, val note: String = "", val noteUpdatedAt: String? = null,
)

@Serializable
data class TrainingNoteRequest(val note: String)

