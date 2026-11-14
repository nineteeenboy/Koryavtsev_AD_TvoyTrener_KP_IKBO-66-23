package com.example.trainer.server.repositories

import com.example.trainer.server.db.DatabaseFactory
import com.example.trainer.server.db.tables.*
import com.example.trainer.server.dto.*
import com.example.trainer.server.services.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.time.Instant

/** Publishes one review per user/trainer after an attended reservation. */
class ReviewRepository(private val db: DatabaseFactory) {
    suspend fun create(uid: String, trainerId: Int, request: ReviewRequest, now: Instant): ReviewDto {
        try {
            return db.query {
                if (Trainers.selectAll().where { Trainers.id eq trainerId }.empty()) notFound("Тренер не найден")
                val attended = Bookings.selectAll().where {
                    (Bookings.userId eq uid) and (Bookings.trainerId eq trainerId) and
                        (Bookings.status eq "ACTIVE") and (Bookings.startsAt lessEq now.minusSeconds(3600))
                }.any()
                if (!attended) conflict("Отзыв доступен после завершения тренировки")
                val id = Reviews.insert {
                    it[userId] = uid; it[Reviews.trainerId] = trainerId
                    it[rating] = request.rating; it[comment] = request.comment
                    it[createdAt] = now
                }[Reviews.id]
                val author = Users.selectAll().where { Users.id eq uid }.single()[Users.name]
                ReviewDto(id, trainerId, author, request.rating, request.comment, now.toString())
            }
        } catch (e: ExposedSQLException) {
            if (e.sqlState == "23505") conflict("Вы уже оставили отзыв об этом тренере")
            throw e
        }
    }
    suspend fun list(trainerId: Int? = null, uid: String? = null): List<ReviewDto> = db.query {
        val query = (Reviews innerJoin Users).selectAll()
        if (trainerId != null) query.andWhere { Reviews.trainerId eq trainerId }
        if (uid != null) query.andWhere { Reviews.userId eq uid }
        query.orderBy(Reviews.createdAt, SortOrder.DESC).map {
            ReviewDto(it[Reviews.id], it[Reviews.trainerId], it[Users.name],
                it[Reviews.rating], it[Reviews.comment], it[Reviews.createdAt].toString())
        }
    }
}

