package com.example.trainer.server.repositories

import com.example.trainer.server.db.DatabaseFactory
import com.example.trainer.server.db.tables.*
import com.example.trainer.server.dto.TrainerDto
import org.jetbrains.exposed.sql.*

/** Reads trainer cards with ratings calculated from actual reviews. */
class TrainerRepository(private val db: DatabaseFactory) {
    suspend fun list(specialization: String?, maxPrice: Double?, minRating: Double?): List<TrainerDto> = db.query {
        val scores = Reviews.selectAll().groupBy { it[Reviews.trainerId] }
        val query = Trainers.selectAll()
        if (!specialization.isNullOrBlank()) query.andWhere { Trainers.specialization eq specialization }
        if (maxPrice != null) query.andWhere { Trainers.price lessEq maxPrice.toBigDecimal() }
        query.orderBy(Trainers.id).map { row ->
            val ratings = scores[row[Trainers.id]].orEmpty().map { it[Reviews.rating] }
            row.toTrainer(if (ratings.isEmpty()) 0.0 else ratings.average(), ratings.size)
        }.filter { minRating == null || it.rating >= minRating }.sortedByDescending { it.isFeatured }
    }
    suspend fun find(id: Int): TrainerDto? = list(null, null, null).firstOrNull { it.id == id }

    private fun ResultRow.toTrainer(rating: Double, count: Int) = TrainerDto(
        this[Trainers.id], this[Trainers.name], this[Trainers.specialization],
        this[Trainers.photoUrl], this[Trainers.description], this[Trainers.experienceYears],
        this[Trainers.price].toDouble(), rating, count, this[Trainers.photoUrl].startsWith("/media/trainers/"),
    )
}

