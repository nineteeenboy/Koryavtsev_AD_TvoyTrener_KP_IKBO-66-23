package com.example.trainer.di

import com.example.trainer.data.repository.*
import com.example.trainer.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds data implementations behind domain interfaces. */
@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun auth(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun trainers(impl: TrainerRepositoryImpl): TrainerRepository
    @Binds @Singleton abstract fun bookings(impl: BookingRepositoryImpl): BookingRepository
    @Binds @Singleton abstract fun reviews(impl: ReviewRepositoryImpl): ReviewRepository
}

