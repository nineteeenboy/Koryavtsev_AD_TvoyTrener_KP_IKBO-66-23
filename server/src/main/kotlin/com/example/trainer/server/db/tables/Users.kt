package com.example.trainer.server.db.tables

import org.jetbrains.exposed.sql.Table

/** Relational users storage. */
object Users : Table("users") {
    val id = varchar("firebase_uid", 128)
    val email = varchar("email", 320)
    val name = varchar("name", 100)
    override val primaryKey = PrimaryKey(id)
}

