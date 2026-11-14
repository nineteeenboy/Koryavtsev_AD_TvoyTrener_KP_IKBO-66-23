package com.example.trainer.server.services

import com.example.trainer.server.dto.*
import com.example.trainer.server.repositories.TrainerWorkspaceRepository

class TrainerWorkspaceService(private val repository: TrainerWorkspaceRepository) {
    suspend fun mine(user: UserDto) = repository.mine(user.id)

    suspend fun submit(user: UserDto, request: TrainerApplicationRequest): TrainerApplicationDto {
        val clean = request.copy(email = request.email.trim(), phone = request.phone.trim(),
            experience = request.experience.trim(), education = request.education.trim(),
            certificates = request.certificates.trim(), specialization = request.specialization.trim())
        if (clean.email.length > 320 || !clean.email.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")))
            badRequest("Введите корректную электронную почту")
        if (clean.phone.length > 40 || clean.phone.count(Char::isDigit) !in 10..15 ||
            clean.phone.any { !it.isDigit() && it !in "+()- " }) badRequest("Введите корректный номер телефона")
        if (clean.experience.length !in 1..5000 || clean.education.length !in 1..5000 || clean.certificates.length > 5000)
            badRequest("Заполните опыт и образование: не более 5000 символов в поле")
        if (clean.specialization.length !in 2..100) badRequest("Специализация должна содержать 2–100 символов")
        if (clean.experienceYears !in 0..80) badRequest("Укажите стаж от 0 до 80 лет")
        if (!clean.price.isFinite() || clean.price < 1 || clean.price > 1_000_000)
            badRequest("Укажите стоимость от 1 до 1 000 000 ₽")
        return repository.submit(user.id, clean)
    }

    private fun requireAdmin(user: UserDto) { if (user.role != "ADMIN") forbidden() }
    private fun trainerId(user: UserDto): Int = user.trainerId ?: forbidden("Доступно только подтверждённым тренерам")
    suspend fun applications(user: UserDto): List<TrainerApplicationDto> {
        requireAdmin(user); return repository.applications()
    }
    suspend fun decide(user: UserDto, id: Int, decision: ApplicationDecisionRequest): TrainerApplicationDto {
        requireAdmin(user)
        if (decision.comment.trim().length > 2000) badRequest("Комментарий не должен превышать 2000 символов")
        if (!decision.approve && decision.comment.isBlank()) badRequest("Укажите причину отказа")
        return repository.decide(user.id, id, decision.copy(comment = decision.comment.trim()))
    }
    suspend fun sessions(user: UserDto) = repository.sessions(trainerId(user))
    suspend fun saveNote(user: UserDto, id: Int, request: TrainingNoteRequest): TrainingSessionDto {
        val trainer = trainerId(user)
        if (request.note.length > 20_000) badRequest("Заметка не должна превышать 20 000 символов")
        return repository.saveNote(trainer, id, request.note.trim())
    }
}
