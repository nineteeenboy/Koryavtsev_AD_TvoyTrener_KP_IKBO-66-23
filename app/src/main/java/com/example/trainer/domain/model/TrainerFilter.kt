package com.example.trainer.domain.model

/** Optional server-side catalog filters. */
data class TrainerFilter(
    val specialization: String? = null,
    val maxPrice: Double? = null,
    val minRating: Double? = null,
)

