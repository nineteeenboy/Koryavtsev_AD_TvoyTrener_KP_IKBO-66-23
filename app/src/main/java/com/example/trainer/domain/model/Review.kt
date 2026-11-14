package com.example.trainer.domain.model

/** Immutable review domain entity, independent of HTTP and Android UI. */
data class Review(val id: Int, val trainerId: Int, val authorName: String, val rating: Int,
    val comment: String, val createdAt: String)

