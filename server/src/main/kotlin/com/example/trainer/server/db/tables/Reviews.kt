package com.example.trainer.server.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

/** Relational reviews storage. */
object Reviews : Table("reviews") {
    val id = integer("id").autoIncrement()
    val userId = reference("user_id", Users.id)
    val trainerId = reference("trainer_id", Trainers.id)
    val rating = integer("rating")
    val comment = varchar("comment", 2000)
    val createdAt = timestamp("created_at")
    init { uniqueIndex(userId, trainerId) }
    override val primaryKey = PrimaryKey(id)
}

