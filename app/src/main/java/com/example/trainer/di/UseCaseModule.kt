package com.example.trainer.di

import com.example.trainer.domain.repository.*
import com.example.trainer.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Constructs pure Kotlin application operations without Android dependencies. */
@Module @InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides fun login(repository: AuthRepository) = LoginUseCase(repository)
    @Provides fun register(repository: AuthRepository) = RegisterUseCase(repository)
    @Provides fun getTrainers(repository: TrainerRepository) = GetTrainersUseCase(repository)
    @Provides fun getTrainerById(repository: TrainerRepository) = GetTrainerByIdUseCase(repository)
    @Provides fun bookTrainer(repository: BookingRepository) = BookTrainerUseCase(repository)
    @Provides fun getMyBookings(repository: BookingRepository) = GetMyBookingsUseCase(repository)
    @Provides fun cancelBooking(repository: BookingRepository) = CancelBookingUseCase(repository)
    @Provides fun getAvailableSlots(repository: BookingRepository) = GetAvailableSlotsUseCase(repository)
    @Provides fun leaveReview(repository: ReviewRepository) = LeaveReviewUseCase(repository)
    @Provides fun getReviews(repository: ReviewRepository) = GetReviewsUseCase(repository)
    @Provides fun getMyReviews(repository: ReviewRepository) = GetMyReviewsUseCase(repository)
    @Provides fun getProfile(repository: AuthRepository) = GetProfileUseCase(repository)
    @Provides fun logout(repository: AuthRepository) = LogoutUseCase(repository)
}

