package com.example.trainer.data.remote.api

import com.example.trainer.data.remote.dto.*
import com.example.trainer.domain.model.TrainerFilter
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import javax.inject.Inject

/** Typed Ktor Client operations matching the server REST contract. */
class TrainerApi @Inject constructor(private val client: HttpClient) {
    suspend fun register(name: String): UserDto = client.post("auth/register") { setBody(RegisterRequest(name)) }.body()
    suspend fun login(): UserDto = client.post("auth/login").body()
    suspend fun profile(): UserDto = client.get("auth/me").body()
    suspend fun trainers(filter: TrainerFilter): List<TrainerDto> = client.get("trainers") {
        filter.specialization?.takeIf { it.isNotBlank() }?.let { parameter("specialization", it) }
        filter.maxPrice?.let { parameter("maxPrice", it) }
        filter.minRating?.let { parameter("minRating", it) }
    }.body()
    suspend fun trainer(id: Int): TrainerDto = client.get("trainers/$id").body()
    suspend fun book(id: Int, startsAt: String): BookingDto =
        client.post("trainers/$id/bookings") { setBody(BookingRequest(startsAt)) }.body()
    suspend fun myBookings(): List<BookingDto> = client.get("bookings/my").body()
    suspend fun cancel(id: Int) { client.delete("bookings/$id") }
    suspend fun slots(id: Int, date: String): List<String> =
        client.get("trainers/$id/slots") { parameter("date", date) }.body<SlotsDto>().startsAt
    suspend fun review(id: Int, rating: Int, comment: String): ReviewDto =
        client.post("trainers/$id/reviews") { setBody(ReviewRequest(rating, comment)) }.body()
    suspend fun reviews(id: Int): List<ReviewDto> = client.get("trainers/$id/reviews").body()
    suspend fun myReviews(): List<ReviewDto> = client.get("reviews/my").body()
    suspend fun myApplications(): List<TrainerApplicationDto> = client.get("trainer-applications/my").body()
    suspend fun submitApplication(request: TrainerApplicationRequest): TrainerApplicationDto =
        client.post("trainer-applications") { setBody(request) }.body()
    suspend fun applications(): List<TrainerApplicationDto> = client.get("admin/trainer-applications").body()
    suspend fun decideApplication(id: Int, approve: Boolean, comment: String): TrainerApplicationDto =
        client.post("admin/trainer-applications/$id/decision") { setBody(ApplicationDecisionRequest(approve, comment)) }.body()
    suspend fun trainingSessions(): List<TrainingSessionDto> = client.get("trainer/bookings").body()
    suspend fun saveTrainingNote(id: Int, note: String): TrainingSessionDto =
        client.put("trainer/bookings/$id/note") { setBody(TrainingNoteRequest(note)) }.body()
}

