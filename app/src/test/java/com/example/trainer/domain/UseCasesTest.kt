package com.example.trainer.domain

import com.example.trainer.domain.model.*
import com.example.trainer.domain.repository.*
import com.example.trainer.domain.usecase.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

/** Verifies every domain operation against repository boundaries and invalid input. */
class UseCasesTest {
    private val auth = mockk<AuthRepository>()
    private val trainers = mockk<TrainerRepository>()
    private val bookings = mockk<BookingRepository>()
    private val reviews = mockk<ReviewRepository>()
    private val user = User("uid", "a@b.ru", "Анна")
    private val trainer = Trainer(1, "Иван", "Бокс", "", "Описание", 5, 1500.0, 4.0, 1)
    private val instant = Instant.now().plusSeconds(86400).toString()
    private val booking = Booking(1, 1, "Иван", instant, "ACTIVE", 1500.0)
    private val review = Review(1, 1, "Анна", 5, "Отлично", Instant.now().toString())

    @Test fun loginTrimsEmail() = runTest {
        coEvery { auth.login("a@b.ru", "secret") } returns user
        assertEquals(user, LoginUseCase(auth)(" a@b.ru ", "secret"))
        coVerify(exactly = 1) { auth.login("a@b.ru", "secret") }
    }
    @Test fun loginRejectsInvalidEmailBeforeNetwork() = runTest {
        expectInvalid { LoginUseCase(auth)("invalid", "secret") }
        coVerify(exactly = 0) { auth.login(any(), any()) }
    }
    @Test fun registerTrimsName() = runTest {
        coEvery { auth.register("Анна", "a@b.ru", "secret") } returns user
        assertEquals(user, RegisterUseCase(auth)(" Анна ", "a@b.ru", "secret"))
    }
    @Test fun registerRejectsShortPassword() = runTest {
        expectInvalid { RegisterUseCase(auth)("Анна", "a@b.ru", "123") }
        coVerify(exactly = 0) { auth.register(any(), any(), any()) }
    }
    @Test fun getTrainersForwardsAllFilters() = runTest {
        val filter = TrainerFilter("Бокс", 2000.0, 4.0)
        coEvery { trainers.getTrainers(filter) } returns listOf(trainer)
        assertEquals(listOf(trainer), GetTrainersUseCase(trainers)(filter))
    }
    @Test fun getTrainersRejectsNanPrice() = runTest {
        expectInvalid { GetTrainersUseCase(trainers)(TrainerFilter(maxPrice = Double.NaN)) }
        coVerify(exactly = 0) { trainers.getTrainers(any()) }
    }
    @Test fun getTrainerByIdReturnsDetail() = runTest {
        coEvery { trainers.getTrainerById(1) } returns trainer
        assertEquals(trainer, GetTrainerByIdUseCase(trainers)(1))
    }
    @Test fun getTrainerRejectsZeroId() = runTest {
        expectInvalid { GetTrainerByIdUseCase(trainers)(0) }
    }
    @Test fun bookTrainerForwardsUtcInstant() = runTest {
        coEvery { bookings.book(1, instant) } returns booking
        assertEquals(booking, BookTrainerUseCase(bookings)(1, instant))
    }
    @Test fun bookRejectsPastTime() = runTest {
        expectInvalid { BookTrainerUseCase(bookings)(1, "2020-01-01T09:00:00Z") }
        coVerify(exactly = 0) { bookings.book(any(), any()) }
    }
    @Test fun getMyBookingsReturnsReservations() = runTest {
        coEvery { bookings.myBookings() } returns listOf(booking)
        assertEquals(listOf(booking), GetMyBookingsUseCase(bookings)())
    }
    @Test fun getMyBookingsPropagatesFailure() = runTest {
        coEvery { bookings.myBookings() } throws IllegalStateException("offline")
        try { GetMyBookingsUseCase(bookings)(); fail("Expected failure") }
        catch (e: IllegalStateException) { assertEquals("offline", e.message) }
    }
    @Test fun cancelBookingDelegates() = runTest {
        coEvery { bookings.cancel(1) } just Runs
        CancelBookingUseCase(bookings)(1)
        coVerify(exactly = 1) { bookings.cancel(1) }
    }
    @Test fun cancelRejectsNegativeId() = runTest { expectInvalid { CancelBookingUseCase(bookings)(-1) } }
    @Test fun slotsForwardsIsoDate() = runTest {
        val date = LocalDate.now().plusDays(1).toString()
        coEvery { bookings.slots(1, date) } returns listOf(instant)
        assertEquals(listOf(instant), GetAvailableSlotsUseCase(bookings)(1, date))
    }
    @Test fun slotsRejectsMalformedDate() = runTest {
        try { GetAvailableSlotsUseCase(bookings)(1, "bad"); fail("Expected invalid date") }
        catch (_: java.time.DateTimeException) { }
        coVerify(exactly = 0) { bookings.slots(any(), any()) }
    }
    @Test fun leaveReviewTrimsComment() = runTest {
        coEvery { reviews.leaveReview(1, 5, "Отлично") } returns review
        assertEquals(review, LeaveReviewUseCase(reviews)(1, 5, " Отлично "))
    }
    @Test fun reviewRejectsInvalidRating() = runTest {
        expectInvalid { LeaveReviewUseCase(reviews)(1, 6, "Отлично") }
        coVerify(exactly = 0) { reviews.leaveReview(any(), any(), any()) }
    }
    @Test fun getReviewsReturnsFeedback() = runTest {
        coEvery { reviews.getReviews(1) } returns listOf(review)
        assertEquals(listOf(review), GetReviewsUseCase(reviews)(1))
    }
    @Test fun getReviewsRejectsInvalidTrainer() = runTest { expectInvalid { GetReviewsUseCase(reviews)(0) } }
    @Test fun getMyReviewsReturnsFeedback() = runTest {
        coEvery { reviews.myReviews() } returns listOf(review)
        assertEquals(listOf(review), GetMyReviewsUseCase(reviews)())
    }
    @Test fun getProfileCanBeSignedOut() = runTest {
        coEvery { auth.currentUser() } returns null
        assertNull(GetProfileUseCase(auth)())
    }
    @Test fun getProfileReturnsUser() = runTest {
        coEvery { auth.currentUser() } returns user
        assertEquals(user, GetProfileUseCase(auth)())
    }
    @Test fun logoutDelegates() = runTest {
        coEvery { auth.logout() } just Runs
        LogoutUseCase(auth)()
        coVerify(exactly = 1) { auth.logout() }
    }
    private suspend fun expectInvalid(block: suspend () -> Unit) {
        try { block(); fail("Expected validation failure") }
        catch (_: IllegalArgumentException) { }
    }
}

