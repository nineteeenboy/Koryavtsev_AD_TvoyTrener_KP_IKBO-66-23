package com.example.trainer.server.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

/** Relational bookings storage. */
object Bookings : Table("bookings") {
    val id = integer("id").autoIncrement()
    val userId = reference("user_id", Users.id)
    val trainerId = reference("trainer_id", Trainers.id)
    val startsAt = timestamp("starts_at")
    val status = varchar("status", 20).default("ACTIVE")
    val price = decimal("price", 10, 2)
    // Cancelled reservations release the unique slot through NULL.
    val activeSlot = timestamp("active_slot").nullable()
    init { uniqueIndex(trainerId, activeSlot); index(false, userId, startsAt) }
    override val primaryKey = PrimaryKey(id)
}

