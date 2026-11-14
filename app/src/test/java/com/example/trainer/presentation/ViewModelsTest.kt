package com.example.trainer.presentation

import com.example.trainer.domain.model.*
import com.example.trainer.domain.repository.*
import com.example.trainer.domain.usecase.*
import com.example.trainer.presentation.auth.AuthViewModel
import com.example.trainer.presentation.trainers.TrainersViewModel
import com.example.trainer.presentation.booking.BookingViewModel
import com.example.trainer.presentation.review.ReviewViewModel
import com.example.trainer.presentation.profile.ProfileViewModel
import com.example.trainer.util.UiState
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Exercises loading, success, empty, failure and cancellation behavior. */
@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsTest {
    @get:Rule val main = MainDispatcherRule()
    private val auth = mockk<AuthRepository>()
    private val trainers = mockk<TrainerRepository>()
    private val bookings = mockk<BookingRepository>()
    private val reviews = mockk<ReviewRepository>()
    private val user = User("uid", "a@b.ru", "Анна")
    private val trainer = Trainer(1, "Иван", "Бокс", "", "Описание", 5, 1500.0, 0.0, 0)

    private fun authVm() = AuthViewModel(LoginUseCase(auth), RegisterUseCase(auth), auth)
    private fun trainersVm() = TrainersViewModel(GetTrainersUseCase(trainers), GetTrainerByIdUseCase(trainers), GetReviewsUseCase(reviews))
    private fun bookingVm() = BookingViewModel(BookTrainerUseCase(bookings), GetMyBookingsUseCase(bookings),
        CancelBookingUseCase(bookings), GetAvailableSlotsUseCase(bookings))

    @Test fun authEmitsLoadingThenUser() = runTest {
        coEvery { auth.login(any(), any()) } coAnswers { delay(100); user }
        val vm = authVm()
        vm.login("a@b.ru", "secret")
        runCurrent()
        assertEquals(UiState.Loading, vm.state.value)
        advanceUntilIdle()
        assertEquals(UiState.Success(user), vm.state.value)
    }
    @Test fun authShowsErrorAndCanRetry() = runTest {
        coEvery { auth.login(any(), any()) } throws IllegalStateException("Недоступно")
        val vm = authVm()
        vm.login("a@b.ru", "secret"); advanceUntilIdle()
        assertEquals(UiState.Error("Недоступно"), vm.state.value)
        coEvery { auth.login(any(), any()) } returns user
        vm.login("a@b.ru", "secret"); advanceUntilIdle()
        assertEquals(UiState.Success(user), vm.state.value)
    }
    @Test fun authCancellationDoesNotBecomeAnError() = runTest {
        coEvery { auth.login(any(), any()) } coAnswers { delay(1000); user }
        val vm = authVm()
        vm.login("a@b.ru", "secret"); runCurrent()
        vm.reset(); advanceUntilIdle()
        assertEquals(UiState.Empty, vm.state.value)
    }
    @Test fun registrationUsesDomainValidation() = runTest {
        val vm = authVm()
        vm.register("А", "a@b.ru", "secret"); advanceUntilIdle()
        assertTrue(vm.state.value is UiState.Error)
        coVerify(exactly = 0) { auth.register(any(), any(), any()) }
    }
    @Test fun emptyCatalogHasEmptyState() = runTest {
        coEvery { trainers.getTrainers(any()) } returns emptyList()
        val vm = trainersVm()
        vm.load(); advanceUntilIdle()
        assertEquals(UiState.Empty, vm.state.value)
    }
    @Test fun newerFilterCancelsOldRequest() = runTest {
        coEvery { trainers.getTrainers(TrainerFilter("Йога")) } coAnswers { delay(1000); emptyList() }
        coEvery { trainers.getTrainers(TrainerFilter("Бокс")) } returns listOf(trainer)
        val vm = trainersVm()
        vm.load(TrainerFilter("Йога")); runCurrent()
        vm.load(TrainerFilter("Бокс")); advanceUntilIdle()
        assertEquals(UiState.Success(listOf(trainer)), vm.state.value)
    }
    @Test fun detailsSurviveReviewFailure() = runTest {
        coEvery { trainers.getTrainerById(1) } returns trainer
        coEvery { reviews.getReviews(1) } throws IllegalStateException("Нет отзывов")
        val vm = trainersVm()
        vm.loadDetail(1); advanceUntilIdle()
        assertEquals(UiState.Success(trainer), vm.detailState.value)
        assertTrue(vm.reviewsState.value is UiState.Error)
    }
    @Test fun bookingShowsServerConflict() = runTest {
        coEvery { bookings.book(any(), any()) } throws IllegalStateException("Это время уже занято")
        val vm = bookingVm()
        vm.reserve(1, java.time.Instant.now().plusSeconds(86400).toString()); advanceUntilIdle()
        assertEquals(UiState.Error("Это время уже занято"), vm.action.value)
    }
    @Test fun bookingLoadsEmptySlots() = runTest {
        coEvery { bookings.slots(1, "2030-01-01") } returns emptyList()
        val vm = bookingVm()
        vm.loadSlots(1, "2030-01-01"); advanceUntilIdle()
        assertEquals(UiState.Empty, vm.slots.value)
    }
    @Test fun cancellationReloadsBookings() = runTest {
        coEvery { bookings.cancel(7) } just Runs
        coEvery { bookings.myBookings() } returns emptyList()
        val vm = bookingVm()
        vm.cancel(7); advanceUntilIdle()
        assertEquals(UiState.Success(Unit), vm.cancelState.value)
        assertEquals(UiState.Empty, vm.state.value)
        coVerify(exactly = 1) { bookings.myBookings() }
    }
    @Test fun reviewDisplaysEligibilityError() = runTest {
        coEvery { reviews.leaveReview(any(), any(), any()) } throws IllegalStateException("Тренировка ещё не завершена")
        val vm = ReviewViewModel(LeaveReviewUseCase(reviews))
        vm.submit(1, 5, "Отлично"); advanceUntilIdle()
        assertTrue(vm.state.value is UiState.Error)
    }
    @Test fun profileLoadsUserAndLogout() = runTest {
        coEvery { auth.currentUser() } returns user
        coEvery { reviews.myReviews() } returns emptyList()
        coEvery { auth.logout() } just Runs
        val vm = ProfileViewModel(GetProfileUseCase(auth), GetMyReviewsUseCase(reviews), LogoutUseCase(auth))
        vm.load(); advanceUntilIdle()
        assertEquals(UiState.Success(user), vm.state.value)
        assertEquals(UiState.Empty, vm.reviewState.value)
        vm.logout(); advanceUntilIdle()
        assertTrue(vm.loggedOut.value)
    }
}

