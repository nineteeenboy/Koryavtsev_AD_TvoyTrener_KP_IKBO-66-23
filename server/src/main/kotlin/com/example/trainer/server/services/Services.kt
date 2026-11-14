package com.example.trainer.server.services

import com.example.trainer.server.auth.FirebasePrincipal
import com.example.trainer.server.dto.*
import com.example.trainer.server.repositories.*
import java.time.*

/** Validates profile synchronization after Firebase authentication. */
class AuthService(private val users: UserRepository) {
    suspend fun register(principal: FirebasePrincipal, name: String): UserDto {
        val clean = name.trim()
        if (clean.length !in 2..100) badRequest("Имя должно содержать 2–100 символов")
        return users.sync(principal.uid, principal.email, clean)
    }
    suspend fun login(principal: FirebasePrincipal) =
        users.sync(principal.uid, principal.email, principal.name)
}
/** Validates trainer filters and resolves missing trainers. */
class TrainerService(private val trainers: TrainerRepository) {
    suspend fun list(specialization: String?, maxPrice: Double?, minRating: Double?): List<TrainerDto> {
        if (maxPrice != null && (!maxPrice.isFinite() || maxPrice < 0)) badRequest("Некорректная цена")
        if (minRating != null && (!minRating.isFinite() || minRating !in 0.0..5.0)) badRequest("Рейтинг должен быть от 0 до 5")
        return trainers.list(specialization, maxPrice, minRating)
    }
    suspend fun get(id: Int) = trainers.find(id) ?: notFound("Тренер не найден")
}
/** Gym works 09:00–21:00 Europe/Moscow; each training lasts one hour. */
class BookingService(
    private val bookings: BookingRepository,
    private val trainers: TrainerService,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val zone = ZoneId.of("Europe/Moscow")
    suspend fun book(uid: String, trainerId: Int, request: BookingRequest): BookingDto {
        val instant = try { Instant.parse(request.startsAt) }
        catch (_: DateTimeException) { badRequest("Ожидается дата ISO-8601 UTC") }
        val local = instant.atZone(zone)
        if (instant <= clock.instant() || instant > clock.instant().plusSeconds(90L * 86400))
            badRequest("Выберите время в ближайшие 90 дней")
        if (local.hour !in 9..20 || local.minute != 0 || local.second != 0 || local.nano != 0)
            badRequest("Тренировки начинаются каждый час с 09:00 до 20:00 МСК")
        return bookings.create(uid, trainerId, instant)
    }
    suspend fun mine(uid: String) = bookings.mine(uid)
    suspend fun cancel(uid: String, id: Int) = bookings.cancel(uid, id, clock.instant())
    suspend fun slots(trainerId: Int, dateText: String): SlotsDto {
        trainers.get(trainerId)
        val date = try { LocalDate.parse(dateText) }
        catch (_: DateTimeException) { badRequest("Ожидается дата YYYY-MM-DD") }
        val today = LocalDate.now(clock.withZone(zone))
        if (date < today || date > today.plusDays(90)) badRequest("Дата за пределами 90 дней")
        val from = date.atStartOfDay(zone).toInstant()
        val occupied = bookings.occupied(trainerId, from, date.plusDays(1).atStartOfDay(zone).toInstant())
        val slots = (9..20).map { date.atTime(it, 0).atZone(zone).toInstant() }
            .filter { it > clock.instant() && it <= clock.instant().plusSeconds(90L * 86400) && it !in occupied }
        return SlotsDto(dateText, zone.id, slots.map(Instant::toString))
    }
}
/** Validates review content before the transactional attendance check. */
class ReviewService(private val reviews: ReviewRepository) {
    suspend fun create(uid: String, trainerId: Int, request: ReviewRequest): ReviewDto {
        val clean = request.comment.trim()
        if (request.rating !in 1..5) badRequest("Оценка должна быть от 1 до 5")
        if (clean.length !in 3..2000) badRequest("Отзыв должен содержать 3–2000 символов")
        return reviews.create(uid, trainerId, request.copy(comment = clean), Instant.now())
    }
    suspend fun list(trainerId: Int) = reviews.list(trainerId = trainerId)
    suspend fun mine(uid: String) = reviews.list(uid = uid)
}


