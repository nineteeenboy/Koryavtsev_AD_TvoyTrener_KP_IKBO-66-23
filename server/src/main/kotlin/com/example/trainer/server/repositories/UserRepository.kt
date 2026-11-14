package com.example.trainer.server.repositories

import com.example.trainer.server.db.DatabaseFactory
import com.example.trainer.server.db.tables.Users
import com.example.trainer.server.db.tables.TrainerAccounts
import com.example.trainer.server.dto.UserDto
import org.jetbrains.exposed.sql.*

/** Persists a profile keyed by verified Firebase UID. */
class UserRepository(private val db: DatabaseFactory,
    private val adminEmails: Set<String> = (System.getenv("ADMIN_EMAILS") ?: "")
        .split(',').map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet(),
) {
    suspend fun sync(uid: String, email: String, name: String?): UserDto = db.query {
        Users.insertIgnore {
            it[id] = uid; it[Users.email] = email
            it[Users.name] = name ?: email.substringBefore('@')
        }
        if (name != null) Users.update({ Users.id eq uid }) {
            it[Users.name] = name; it[Users.email] = email
        }
        val row = Users.selectAll().where { Users.id eq uid }.single()
        val trainerId = TrainerAccounts.selectAll().where { TrainerAccounts.userId eq uid }
            .singleOrNull()?.get(TrainerAccounts.trainerId)
        UserDto(row[Users.id], email, row[Users.name],
            if (email.lowercase() in adminEmails) "ADMIN" else if (trainerId != null) "TRAINER" else "CLIENT", trainerId)
    }
}

