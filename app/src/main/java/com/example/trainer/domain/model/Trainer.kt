package com.example.trainer.domain.model

/** Immutable trainer domain entity, independent of HTTP and Android UI. */
data class Trainer(val id: Int, val name: String, val specialization: String, val photoUrl: String,
    val description: String, val experienceYears: Int, val price: Double,
    val rating: Double, val reviewCount: Int, val isFeatured: Boolean = false)

