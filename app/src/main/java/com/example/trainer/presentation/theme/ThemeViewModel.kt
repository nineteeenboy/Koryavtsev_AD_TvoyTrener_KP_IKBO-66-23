package com.example.trainer.presentation.theme

import androidx.lifecycle.*
import com.example.trainer.data.repository.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Exposes persistent application appearance state. */
@HiltViewModel
class ThemeViewModel @Inject constructor(private val preferences: ThemePreferences) : ViewModel() {
    val dark = preferences.dark.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    fun setDark(value: Boolean) { viewModelScope.launch { preferences.setDark(value) } }
}

