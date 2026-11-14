package com.example.trainer.server.repositories

import com.example.trainer.server.db.DatabaseFactory
import com.example.trainer.server.db.tables.*
import com.example.trainer.server.dto.BookingDto
import com.example.trainer.server.services.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.time.Instant

/** Transactional bookings; a unique nullable active slot prevents competing reservations. */
class BookingRepository(private val db: DatabaseFactory) {
    suspend fun create(uid: String, trainerId: Int, startsAt: Instant): BookingDto {
        try {
            return db.query {
                val trainer = Trainers.selectAll().where { Trainers.id eq trainerId }.singleOrNull()
                    ?: notFound("Тренер не найден")
                val id = Bookings.insert {
                    it[userId] = uid; it[Bookings.trainerId] = trainerId
                    it[Bookings.startsAt] = startsAt; it[activeSlot] = startsAt
                    it[price] = trainer[Trainers.price]
                }[Bookings.id]
                BookingDto(id, trainerId, trainer[Trainers.name], startsAt.toString(), "ACTIVE", trainer[Trainers.price].toDouble())
            }
        } catch (e: ExposedSQLException) {
            if (e.sqlState == "23505") conflict("Это время уже занято")
            throw e
        }
    }
    suspend fun mine(uid: String): List<BookingDto> = db.query {
        (Bookings innerJoin Trainers).selectAll().where { Bookings.userId eq uid }
            .orderBy(Bookings.startsAt, SortOrder.DESC).map {
                BookingDto(it[Bookings.id], it[Bookings.trainerId], it[Trainers.name],
                    it[Bookings.startsAt].toString(), it[Bookings.status], it[Bookings.price].toDouble())
            }
    }
    suspend fun cancel(uid: String, id: Int, now: Instant) = db.query {
        val count = Bookings.update({
            (Bookings.id eq id) and (Bookings.userId eq uid) and
                (Bookings.status eq "ACTIVE") and (Bookings.startsAt greater now)
        }) { it[status] = "CANCELLED"; it[activeSlot] = null }
        if (count == 0) conflict("Запись не найдена, уже отменена или началась")
    }
    suspend fun occupied(trainerId: Int, from: Instant, to: Instant): Set<Instant> = db.query {
        Bookings.selectAll().where {
            (Bookings.trainerId eq trainerId) and (Bookings.status eq "ACTIVE") and
                (Bookings.startsAt greaterEq from) and (Bookings.startsAt less to)
        }.map { it[Bookings.startsAt] }.toSet()
    }
}

