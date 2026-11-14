package com.example.trainer.server.db.tables

import org.jetbrains.exposed.sql.Table

/** Relational trainers storage. */
object Trainers : Table("trainers") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 120)
    val specialization = varchar("specialization", 100)
    val photoUrl = text("photo_url")
    val description = text("description")
    val experienceYears = integer("experience_years")
    val price = decimal("price", 10, 2)
    override val primaryKey = PrimaryKey(id)
}

