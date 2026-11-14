package com.example.trainer.server.repositories

import com.example.trainer.server.db.DatabaseFactory
import com.example.trainer.server.db.tables.*
import com.example.trainer.server.dto.*
import com.example.trainer.server.services.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.time.Instant

class TrainerWorkspaceRepository(private val db: DatabaseFactory) {
    private fun ResultRow.application() = TrainerApplicationDto(
        this[TrainerApplications.id], this[TrainerApplications.userId], this[Users.name],
        this[TrainerApplications.email], this[TrainerApplications.phone],
        this[TrainerApplications.specialization], this[TrainerApplications.experienceYears],
        this[TrainerApplications.price].toDouble(), this[TrainerApplications.experience],
        this[TrainerApplications.education], this[TrainerApplications.certificates],
        this[TrainerApplications.status], this[TrainerApplications.submittedAt].toString(),
        this[TrainerApplications.reviewedAt]?.toString(), this[TrainerApplications.adminComment],
    )

    suspend fun mine(uid: String): List<TrainerApplicationDto> = db.query {
        TrainerApplications.join(Users, JoinType.INNER, TrainerApplications.userId, Users.id).selectAll()
            .where { TrainerApplications.userId eq uid }.map { it.application() }
    }

    suspend fun applications(): List<TrainerApplicationDto> = db.query {
        TrainerApplications.join(Users, JoinType.INNER, TrainerApplications.userId, Users.id).selectAll()
            .orderBy(TrainerApplications.submittedAt, SortOrder.DESC).map { it.application() }
    }

    suspend fun submit(uid: String, request: TrainerApplicationRequest): TrainerApplicationDto {
        try {
            return db.query {
                // Lock the account so concurrent submissions cannot create two applications.
                Users.selectAll().where { Users.id eq uid }.forUpdate().single()
                if (TrainerAccounts.selectAll().where { TrainerAccounts.userId eq uid }.any())
                    conflict("У вас уже есть доступ тренера")
                val existing = TrainerApplications.selectAll().where { TrainerApplications.userId eq uid }.singleOrNull()
                if (existing != null && existing[TrainerApplications.status] != "REJECTED")
                    conflict("Заявка уже отправлена")
                fun fill(statement: org.jetbrains.exposed.sql.statements.UpdateBuilder<*>) {
                    statement[TrainerApplications.email] = request.email
                    statement[TrainerApplications.phone] = request.phone
                    statement[TrainerApplications.specialization] = request.specialization
                    statement[TrainerApplications.experienceYears] = request.experienceYears
                    statement[TrainerApplications.price] = request.price.toBigDecimal()
                    statement[TrainerApplications.experience] = request.experience
                    statement[TrainerApplications.education] = request.education
                    statement[TrainerApplications.certificates] = request.certificates
                    statement[TrainerApplications.status] = "PENDING"
                    statement[TrainerApplications.submittedAt] = Instant.now()
                    statement[TrainerApplications.reviewedAt] = null
                    statement[TrainerApplications.reviewerId] = null
                    statement[TrainerApplications.adminComment] = ""
                }
                val id = if (existing == null) TrainerApplications.insert {
                    it[userId] = uid; fill(it)
                }[TrainerApplications.id] else existing[TrainerApplications.id].also { id ->
                    TrainerApplications.update({ TrainerApplications.id eq id }) { fill(it) }
                }
                TrainerApplications.join(Users, JoinType.INNER, TrainerApplications.userId, Users.id).selectAll()
                    .where { TrainerApplications.id eq id }.single().application()
            }
        } catch (e: ExposedSQLException) {
            if (e.sqlState == "23505") conflict("Заявка уже отправлена")
            throw e
        }
    }

    suspend fun decide(adminUid: String, id: Int, decision: ApplicationDecisionRequest): TrainerApplicationDto = db.query {
        val row = TrainerApplications.selectAll().where { TrainerApplications.id eq id }.forUpdate()
            .singleOrNull() ?: notFound("Заявка не найдена")
        if (row[TrainerApplications.status] != "PENDING") conflict("Заявка уже рассмотрена")
        if (decision.approve) {
            val uid = row[TrainerApplications.userId]
            val user = Users.selectAll().where { Users.id eq uid }.single()
            val trainerId = Trainers.insert {
                it[name] = user[Users.name]
                it[specialization] = row[TrainerApplications.specialization]
                it[photoUrl] = "/media/trainers/default.png"
                it[description] = "Опыт работы\n${row[TrainerApplications.experience]}\n\n" +
                    "Образование\n${row[TrainerApplications.education]}" +
                    row[TrainerApplications.certificates].takeIf { it.isNotBlank() }?.let { "\n\nСертификаты\n$it" }.orEmpty()
                it[experienceYears] = row[TrainerApplications.experienceYears]
                it[price] = row[TrainerApplications.price]
            }[Trainers.id]
            TrainerAccounts.insert { it[userId] = uid; it[TrainerAccounts.trainerId] = trainerId }
        }
        TrainerApplications.update({ TrainerApplications.id eq id }) {
            it[status] = if (decision.approve) "APPROVED" else "REJECTED"
            it[reviewedAt] = Instant.now(); it[reviewerId] = adminUid; it[adminComment] = decision.comment
        }
        TrainerApplications.join(Users, JoinType.INNER, TrainerApplications.userId, Users.id).selectAll()
            .where { TrainerApplications.id eq id }.single().application()
    }

    private fun ResultRow.session() = TrainingSessionDto(
        this[Bookings.id], this[Users.name], this[Bookings.startsAt].toString(),
        this[Bookings.status], this[Bookings.price].toDouble(),
        this.getOrNull(TrainingNotes.note).orEmpty(), this.getOrNull(TrainingNotes.updatedAt)?.toString(),
    )

    suspend fun sessions(trainerId: Int): List<TrainingSessionDto> = db.query {
        ((Bookings innerJoin Users) leftJoin TrainingNotes).selectAll()
            .where { Bookings.trainerId eq trainerId }.orderBy(Bookings.startsAt, SortOrder.DESC)
            .map { it.session() }
    }

    suspend fun saveNote(trainerId: Int, bookingId: Int, note: String): TrainingSessionDto = db.query {
        // Scope and lock by trainer identity, never by an ID supplied as a trainer ID by a client.
        Bookings.selectAll().where { (Bookings.id eq bookingId) and (Bookings.trainerId eq trainerId) }
            .forUpdate().singleOrNull() ?: notFound("Запись не найдена")
        val now = Instant.now()
        TrainingNotes.upsert(TrainingNotes.bookingId) {
            it[TrainingNotes.bookingId] = bookingId; it[TrainingNotes.note] = note; it[updatedAt] = now
        }
        ((Bookings innerJoin Users) leftJoin TrainingNotes).selectAll()
            .where { Bookings.id eq bookingId }.single().session()
    }
}

