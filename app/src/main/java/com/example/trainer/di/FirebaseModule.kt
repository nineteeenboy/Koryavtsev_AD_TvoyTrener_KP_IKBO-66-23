package com.example.trainer.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Supplies Firebase SDK's process-wide authentication instance. */
@Module @InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides @Singleton fun firebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}

