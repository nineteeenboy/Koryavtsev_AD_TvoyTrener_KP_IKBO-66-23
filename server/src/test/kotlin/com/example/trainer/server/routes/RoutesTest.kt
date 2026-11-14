package com.example.trainer.server.routes

import com.example.trainer.server.auth.*
import com.example.trainer.server.db.*
import com.example.trainer.server.db.tables.*
import com.example.trainer.server.dto.*
import com.example.trainer.server.plugins.*
import com.example.trainer.server.repositories.*
import com.example.trainer.server.services.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.*
import io.mockk.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert.*
import org.junit.Test
import java.time.*
import java.util.UUID

/** Real REST/SQL integration; only the Firebase network boundary is mocked. */
class RoutesTest {
    private fun withApi(test: suspend ApplicationTestBuilder.(DatabaseFactory) -> Unit) = testApplication {
        environment { config = MapApplicationConfig() }
        val db = DatabaseFactory(Database.connect(
            "jdbc:h2:mem:${UUID.randomUUID()};MODE=PostgreSQL;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver",
        ))
        db.createTables()
        seed(db)
        val verifier = mockk<TokenVerifier>()
        coEvery { verifier.verifyToken("valid") } returns FirebasePrincipal("user1", "a@b.ru", "Анна")
        coEvery { verifier.verifyToken("other") } returns FirebasePrincipal("user2", "b@b.ru", "Борис")
        coEvery { verifier.verifyToken("admin") } returns FirebasePrincipal("admin1", "admin@example.com", "Администратор")
        coEvery { verifier.verifyToken("trainer2") } returns FirebasePrincipal("trainer2", "trainer2@example.com", "Второй тренер")
        coEvery { verifier.verifyToken("expired") } returns null
        val auth = AuthService(UserRepository(db, setOf("admin@example.com")))
        val trainers = TrainerService(TrainerRepository(db))
        val bookings = BookingService(BookingRepository(db), trainers)
        val reviews = ReviewService(ReviewRepository(db))
        application {
            configureSerialization()
            configureStatusPages()
            configureSecurity(verifier)
            configureRouting(auth, trainers, bookings, reviews, TrainerWorkspaceService(TrainerWorkspaceRepository(db)))
        }
        test(db)
        coVerify(atLeast = 0) { verifier.verifyToken(any()) }
    }
    private fun ApplicationTestBuilder.jsonClient() = createClient {
        install(ContentNegotiation) { json() }
    }
    private fun applicationRequest() = TrainerApplicationRequest(
        "trainer@example.com", "+7 (999) 123-45-67", "Пять лет персональных тренировок",
        "Университет физической культуры", "Курсы силовой подготовки", "Силовая подготовка", 5, 2500.0,
    )
    @Test fun applicationPersistsAndOnlyAdminCanApprove() = withApi {
        val client = jsonClient()
        assertEquals(HttpStatusCode.Unauthorized, client.get("/trainer-applications/my").status)
        val submitted = client.post("/trainer-applications") {
            bearerAuth("valid"); contentType(ContentType.Application.Json); setBody(applicationRequest())
        }
        assertEquals(HttpStatusCode.Created, submitted.status)
        val application = submitted.body<TrainerApplicationDto>()
        assertEquals("PENDING", application.status)
        assertEquals(application, client.get("/trainer-applications/my") { bearerAuth("valid") }
            .body<List<TrainerApplicationDto>>().single())
        assertTrue(client.get("/trainer-applications/my") { bearerAuth("other") }.body<List<TrainerApplicationDto>>().isEmpty())
        assertEquals(HttpStatusCode.Forbidden, client.get("/admin/trainer-applications") { bearerAuth("valid") }.status)
        assertEquals(HttpStatusCode.Forbidden, client.post("/admin/trainer-applications/${application.id}/decision") {
            bearerAuth("valid"); contentType(ContentType.Application.Json); setBody(ApplicationDecisionRequest(true))
        }.status)
        assertEquals("ADMIN", client.get("/auth/me") { bearerAuth("admin") }.body<UserDto>().role)
        assertEquals(1, client.get("/admin/trainer-applications") { bearerAuth("admin") }.body<List<TrainerApplicationDto>>().size)
        val approved = client.post("/admin/trainer-applications/${application.id}/decision") {
            bearerAuth("admin"); contentType(ContentType.Application.Json); setBody(ApplicationDecisionRequest(true, "Добро пожаловать"))
        }
        assertEquals("APPROVED", approved.body<TrainerApplicationDto>().status)
        val user = client.get("/auth/me") { bearerAuth("valid") }.body<UserDto>()
        assertEquals("TRAINER", user.role); assertNotNull(user.trainerId)
        val trainer = client.get("/trainers/${user.trainerId}").body<TrainerDto>()
        assertEquals("Анна", trainer.name); assertEquals("Силовая подготовка", trainer.specialization)
        assertEquals(2500.0, trainer.price, 0.01)
        assertEquals(HttpStatusCode.OK, client.get(trainer.photoUrl).status)
        assertEquals(HttpStatusCode.Conflict, client.post("/admin/trainer-applications/${application.id}/decision") {
            bearerAuth("admin"); contentType(ContentType.Application.Json); setBody(ApplicationDecisionRequest(true))
        }.status)
        assertEquals(18, client.get("/trainers").body<List<TrainerDto>>().size)
    }
    @Test fun rejectionRequiresReasonAndCanBeResubmitted() = withApi {
        val client = jsonClient()
        val application = client.post("/trainer-applications") {
            bearerAuth("valid"); contentType(ContentType.Application.Json); setBody(applicationRequest())
        }.body<TrainerApplicationDto>()
        assertEquals(HttpStatusCode.Conflict, client.post("/trainer-applications") {
            bearerAuth("valid"); contentType(ContentType.Application.Json); setBody(applicationRequest())
        }.status)
        assertEquals(HttpStatusCode.BadRequest, client.post("/admin/trainer-applications/${application.id}/decision") {
            bearerAuth("admin"); contentType(ContentType.Application.Json); setBody(ApplicationDecisionRequest(false))
        }.status)
        client.post("/admin/trainer-applications/${application.id}/decision") {
            bearerAuth("admin"); contentType(ContentType.Application.Json); setBody(ApplicationDecisionRequest(false, "Уточните образование"))
        }
        val rejected = client.get("/trainer-applications/my") { bearerAuth("valid") }.body<List<TrainerApplicationDto>>().single()
        assertEquals("REJECTED", rejected.status); assertEquals("Уточните образование", rejected.adminComment)
        assertEquals("CLIENT", client.get("/auth/me") { bearerAuth("valid") }.body<UserDto>().role)
        val revised = client.post("/trainer-applications") {
            bearerAuth("valid"); contentType(ContentType.Application.Json)
            setBody(applicationRequest().copy(education = "Диплом бакалавра физической культуры"))
        }.body<TrainerApplicationDto>()
        assertEquals(application.id, revised.id); assertEquals("PENDING", revised.status)
        assertNull(revised.reviewedAt); assertEquals("", revised.adminComment)
    }
    @Test fun applicationsRejectInvalidFieldsAndClientRoleInjection() = withApi {
        val client = jsonClient()
        for (request in listOf(applicationRequest().copy(phone = "123"), applicationRequest().copy(email = "bad"),
            applicationRequest().copy(price = -1.0), applicationRequest().copy(experienceYears = 81),
            applicationRequest().copy(education = " "), applicationRequest().copy(certificates = "x".repeat(5001)))) {
            assertEquals(HttpStatusCode.BadRequest, client.post("/trainer-applications") {
                bearerAuth("valid"); contentType(ContentType.Application.Json); setBody(request)
            }.status)
        }
        val response = client.post("/auth/register") {
            bearerAuth("valid"); contentType(ContentType.Application.Json)
            setBody("""{"name":"Анна","role":"ADMIN","trainerId":1}""")
        }
        assertEquals("CLIENT", response.body<UserDto>().role)
        assertEquals(HttpStatusCode.Forbidden, client.get("/trainer/bookings") { bearerAuth("valid") }.status)
    }
    @Test fun trainerDiaryScopesRecordsAndPersistsPrivateNotes() = withApi {
        val client = jsonClient()
        suspend fun approve(token: String): Int {
            val application = client.post("/trainer-applications") {
                bearerAuth(token); contentType(ContentType.Application.Json); setBody(applicationRequest())
            }.body<TrainerApplicationDto>()
            client.post("/admin/trainer-applications/${application.id}/decision") {
                bearerAuth("admin"); contentType(ContentType.Application.Json); setBody(ApplicationDecisionRequest(true))
            }
            return client.get("/auth/me") { bearerAuth(token) }.body<UserDto>().trainerId!!
        }
        val trainerId = approve("valid")
        approve("trainer2")
        val date = LocalDate.now(ZoneId.of("Europe/Moscow")).plusDays(1)
        val slot = client.get("/trainers/$trainerId/slots?date=$date").body<SlotsDto>().startsAt.first()
        val booking = client.post("/trainers/$trainerId/bookings") {
            bearerAuth("other"); contentType(ContentType.Application.Json); setBody(BookingRequest(slot))
        }.body<BookingDto>()
        val session = client.get("/trainer/bookings") { bearerAuth("valid") }.body<List<TrainingSessionDto>>().single()
        assertEquals("Борис", session.clientName); assertEquals("", session.note)
        assertTrue(client.get("/trainer/bookings") { bearerAuth("trainer2") }.body<List<TrainingSessionDto>>().isEmpty())
        assertEquals(HttpStatusCode.NotFound, client.put("/trainer/bookings/${booking.id}/note") {
            bearerAuth("trainer2"); contentType(ContentType.Application.Json); setBody(TrainingNoteRequest("Чужая заметка"))
        }.status)
        assertEquals(HttpStatusCode.Forbidden, client.put("/trainer/bookings/${booking.id}/note") {
            bearerAuth("other"); contentType(ContentType.Application.Json); setBody(TrainingNoteRequest("Подмена"))
        }.status)
        val note = "Приседания: 3 × 10, 40 кг. Следующий раз: 45 кг."
        assertEquals(HttpStatusCode.OK, client.put("/trainer/bookings/${booking.id}/note") {
            bearerAuth("valid"); contentType(ContentType.Application.Json); setBody(TrainingNoteRequest(note))
        }.status)
        val loaded = client.get("/trainer/bookings") { bearerAuth("valid") }.body<List<TrainingSessionDto>>().single()
        assertEquals(note, loaded.note); assertNotNull(loaded.noteUpdatedAt)
        assertFalse(client.get("/bookings/my") { bearerAuth("other") }.body<String>().contains(note))
        client.put("/trainer/bookings/${booking.id}/note") {
            bearerAuth("valid"); contentType(ContentType.Application.Json); setBody(TrainingNoteRequest("Обновлено"))
        }
        assertEquals("Обновлено", client.get("/trainer/bookings") { bearerAuth("valid") }.body<List<TrainingSessionDto>>().single().note)
        assertEquals(HttpStatusCode.BadRequest, client.put("/trainer/bookings/${booking.id}/note") {
            bearerAuth("valid"); contentType(ContentType.Application.Json); setBody(TrainingNoteRequest("x".repeat(20001)))
        }.status)
        client.delete("/bookings/${booking.id}") { bearerAuth("other") }
        assertEquals("Обновлено", client.get("/trainer/bookings") { bearerAuth("valid") }.body<List<TrainingSessionDto>>().single().note)
    }
    @Test fun trainersAreSeededAndFiltered() = withApi {
        val client = jsonClient()
        val response = client.get("/trainers")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(17, response.body<List<TrainerDto>>().size)
        val filtered = client.get("/trainers?specialization=Функциональный тренинг&maxPrice=1600").body<List<TrainerDto>>()
        assertEquals(1, filtered.size)
        assertEquals("Функциональный тренинг", filtered.single().specialization)
        assertEquals(HttpStatusCode.BadRequest, client.get("/trainers?minRating=NaN").status)
        assertEquals(HttpStatusCode.NotFound, client.get("/trainers/999").status)
    }
    @Test fun authRequiresVerifiedFirebaseToken() = withApi {
        val client = jsonClient()
        assertEquals(HttpStatusCode.Unauthorized, client.post("/auth/login").status)
        assertEquals(HttpStatusCode.Unauthorized, client.post("/auth/login") { bearerAuth("expired") }.status)
        val registered = client.post("/auth/register") {
            bearerAuth("valid"); contentType(ContentType.Application.Json); setBody(RegisterRequest("Анна"))
        }
        assertEquals(HttpStatusCode.OK, registered.status)
        assertEquals("user1", registered.body<UserDto>().id)
        val loggedIn = client.post("/auth/login") { bearerAuth("valid") }
        assertEquals("Анна", loggedIn.body<UserDto>().name)
        assertEquals(HttpStatusCode.BadRequest, client.post("/auth/register") {
            bearerAuth("valid"); contentType(ContentType.Application.Json); setBody(RegisterRequest("А"))
        }.status)
    }
    @Test fun bookingConflictOwnershipCancellationAndSlotRelease() = withApi {
        val client = jsonClient()
        val date = LocalDate.now(ZoneId.of("Europe/Moscow")).plusDays(1).toString()
        val slot = client.get("/trainers/1/slots?date=$date").body<SlotsDto>().startsAt.first()
        suspend fun reserve(token: String) = client.post("/trainers/1/bookings") {
            bearerAuth(token); contentType(ContentType.Application.Json); setBody(BookingRequest(slot))
        }
        val created = reserve("valid")
        assertEquals(HttpStatusCode.Created, created.status)
        val booking = created.body<BookingDto>()
        assertEquals(HttpStatusCode.Conflict, reserve("other").status)
        assertEquals(1, client.get("/bookings/my") { bearerAuth("valid") }.body<List<BookingDto>>().size)
        assertEquals(0, client.get("/bookings/my") { bearerAuth("other") }.body<List<BookingDto>>().size)
        assertEquals(HttpStatusCode.Conflict, client.delete("/bookings/${booking.id}") { bearerAuth("other") }.status)
        assertEquals(HttpStatusCode.NoContent, client.delete("/bookings/${booking.id}") { bearerAuth("valid") }.status)
        assertEquals(HttpStatusCode.Created, reserve("other").status)
    }
    @Test fun reviewsRequireAttendanceAndUpdateRating() = withApi { db ->
        val client = jsonClient()
        client.post("/auth/login") { bearerAuth("valid") }
        suspend fun publish(rating: Int = 5) = client.post("/trainers/1/reviews") {
            bearerAuth("valid"); contentType(ContentType.Application.Json); setBody(ReviewRequest(rating, "Отличная тренировка"))
        }
        assertEquals(HttpStatusCode.Conflict, publish().status)
        assertEquals(HttpStatusCode.BadRequest, publish(6).status)
        transaction(db.database) {
            Bookings.insert {
                it[userId] = "user1"; it[trainerId] = 1
                it[startsAt] = Instant.now().minusSeconds(7200)
                it[price] = 1500.toBigDecimal()
            }
        }
        assertEquals(HttpStatusCode.Created, publish().status)
        assertEquals(HttpStatusCode.Conflict, publish().status)
        val trainer = client.get("/trainers/1").body<TrainerDto>()
        assertEquals(5.0, trainer.rating, 0.001)
        assertEquals(1, trainer.reviewCount)
        assertEquals(1, client.get("/reviews/my") { bearerAuth("valid") }.body<List<ReviewDto>>().size)
    }
    @Test fun malformedBookingsReturnJsonErrors() = withApi {
        val client = jsonClient()
        val response = client.post("/trainers/1/bookings") {
            bearerAuth("valid"); contentType(ContentType.Application.Json); setBody(BookingRequest("bad"))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("INVALID_REQUEST", response.body<ErrorDto>().code)
    }
    @Test fun ddxCatalogHasMatchingBundledPortraitsAndStableIds() = withApi { db ->
        val client = jsonClient()
        val before = client.get("/trainers").body<List<TrainerDto>>()
        val ddx = before.filter { it.photoUrl.startsWith("/media/ddx/") }
        assertEquals(12, ddx.size)
        assertTrue(ddx.any { it.name == "Алексей Кравченко" && it.experienceYears == 22 })
        ddx.forEach { trainer ->
            assertFalse(trainer.isFeatured)
            assertTrue(trainer.description.contains("https://www.ddxfitness.ru/coaches/"))
            val photo = client.get(trainer.photoUrl)
            assertEquals(HttpStatusCode.OK, photo.status)
            assertTrue(photo.body<ByteArray>().size > 1000)
        }
        seed(db)
        seed(db)
        val after = client.get("/trainers").body<List<TrainerDto>>()
        assertEquals(before, after)
        assertFalse(after.any { it.photoUrl.contains("pravatar") })
    }

    @Test fun legacyReplacementKeepsBookingsReviewsAndSuppliedProfiles() = withApi { db ->
        val client = jsonClient()
        client.post("/auth/login") { bearerAuth("valid") }
        val before = client.get("/trainers").body<List<TrainerDto>>()
        val trainer = before.first { it.name == "Алексей Кравченко" }
        transaction(db.database) {
            Trainers.update({ Trainers.id eq trainer.id }) {
                it[name] = "Алексей Орлов"
                it[photoUrl] = "https://i.pravatar.cc/400?img=11"
            }
            Bookings.insert {
                it[userId] = "user1"; it[trainerId] = trainer.id
                it[startsAt] = Instant.now().minusSeconds(7200)
                it[price] = 1500.toBigDecimal()
            }
            Reviews.insert {
                it[userId] = "user1"; it[trainerId] = trainer.id
                it[rating] = 5; it[comment] = "Сохранённый отзыв"
                it[createdAt] = Instant.now()
            }
        }
        seed(db)
        seed(db)
        val after = client.get("/trainers").body<List<TrainerDto>>()
        assertEquals(17, after.size)
        val updated = after.single { it.id == trainer.id }
        assertEquals(trainer.name, updated.name)
        assertEquals(trainer.photoUrl, updated.photoUrl)
        assertEquals(trainer.price, updated.price, 0.001)
        assertEquals(1, updated.reviewCount)
        assertEquals(before.filter { it.isFeatured }, after.filter { it.isFeatured })
        assertEquals(trainer.id, client.get("/bookings/my") { bearerAuth("valid") }
            .body<List<BookingDto>>().single().trainerId)
        assertEquals("Сохранённый отзыв", client.get("/reviews/my") { bearerAuth("valid") }
            .body<List<ReviewDto>>().single().comment)
    }

    @Test fun suppliedProfilesAndPhotosArePublicAndReseedingPreservesIds() = withApi { db ->
        val client = jsonClient()
        val before = client.get("/trainers").body<List<TrainerDto>>().filter { it.isFeatured }
        assertEquals(5, before.size)
        seedFeaturedTrainers(db)
        seedFeaturedTrainers(db)
        val after = client.get("/trainers").body<List<TrainerDto>>()
        assertEquals(17, after.size)
        assertEquals(before.map { it.id }, after.filter { it.isFeatured }.map { it.id })
        val strength = after.first { it.photoUrl.endsWith("trainer_1.jpg") }
        val detail = client.get("/trainers/${strength.id}").body<TrainerDto>()
        assertTrue(detail.description.contains("Учёный фитнес"))
        assertTrue(detail.description.contains("Люберцы"))
        assertTrue(detail.description.contains("2018"))
        val photo = client.get(detail.photoUrl)
        assertEquals(HttpStatusCode.OK, photo.status)
        assertEquals("image/jpeg", photo.headers[HttpHeaders.ContentType]?.substringBefore(';'))
        val bytes = photo.body<ByteArray>()
        assertTrue(bytes.size > 1000)
        assertEquals(0xFF.toByte(), bytes[0])
        assertEquals(0xD8.toByte(), bytes[1])
    }
}

