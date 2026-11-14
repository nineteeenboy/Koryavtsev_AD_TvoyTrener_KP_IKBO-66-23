package com.example.trainer.domain.model

/** Immutable user domain entity, independent of HTTP and Android UI. */
data class User(val id: String, val email: String, val name: String,
    val role: String = "CLIENT", val trainerId: Int? = null)

