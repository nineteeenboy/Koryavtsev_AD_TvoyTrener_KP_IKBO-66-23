package com.example.trainer.data.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeStore by preferencesDataStore("theme")

/** Persists the explicit appearance setting; null follows the system. */
@Singleton
class ThemePreferences @Inject constructor(@ApplicationContext private val context: Context) {
    private val key = booleanPreferencesKey("dark")
    val dark: Flow<Boolean?> = context.themeStore.data.map { it[key] }
    suspend fun setDark(value: Boolean) { context.themeStore.edit { it[key] = value } }
}

