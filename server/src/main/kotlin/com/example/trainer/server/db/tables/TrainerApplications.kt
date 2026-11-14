package com.example.trainer.server.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

/** One application per account; rejected applications can be revised and resubmitted. */
object TrainerApplications : Table("trainer_applications") {
    val id = integer("id").autoIncrement()
    val userId = reference("user_id", Users.id).uniqueIndex()
    val email = varchar("email", 320)
    val phone = varchar("phone", 40)
    val specialization = varchar("specialization", 100)
    val experienceYears = integer("experience_years")
    val price = decimal("price", 10, 2)
    val experience = text("experience")
    val education = text("education")
    val certificates = text("certificates")
    val status = varchar("status", 20).default("PENDING")
    val submittedAt = timestamp("submitted_at")
    val reviewedAt = timestamp("reviewed_at").nullable()
    val reviewerId = reference("reviewer_id", Users.id).nullable()
    val adminComment = varchar("admin_comment", 2000).default("")
    override val primaryKey = PrimaryKey(id)
}

/** A trainer's catalog identity is assigned exclusively by an administrator. */
object TrainerAccounts : Table("trainer_accounts") {
    val userId = reference("user_id", Users.id)
    val trainerId = reference("trainer_id", Trainers.id).uniqueIndex()
    override val primaryKey = PrimaryKey(userId)
}

/** Private training diary; notes never enter the public or client booking responses. */
object TrainingNotes : Table("training_notes") {
    val bookingId = reference("booking_id", Bookings.id)
    val note = text("note")
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(bookingId)
}
