package com.example.trainer.presentation.profile

import androidx.lifecycle.*
import com.example.trainer.domain.model.*
import com.example.trainer.domain.usecase.*
import com.example.trainer.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/** Loads the user profile and personal review history, and signs out. */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profile: GetProfileUseCase,
    private val reviews: GetMyReviewsUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {
    private val mutable = MutableStateFlow<UiState<User>>(UiState.Loading)
    val state = mutable.asStateFlow()
    private val reviewList = MutableStateFlow<UiState<List<Review>>>(UiState.Loading)
    val reviewState = reviewList.asStateFlow()
    private val signedOut = MutableStateFlow(false)
    val loggedOut = signedOut.asStateFlow()
    fun load() {
        viewModelScope.launch {
            mutable.value = UiState.Loading
            try { mutable.value = profile()?.let { UiState.Success(it) } ?: UiState.Empty }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutable.value = UiState.Error(e.userMessage()) }
            try {
                val result = reviews()
                reviewList.value = if (result.isEmpty()) UiState.Empty else UiState.Success(result)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { reviewList.value = UiState.Error(e.userMessage()) }
        }
    }
    fun logout() {
        viewModelScope.launch {
            try { logoutUseCase(); signedOut.value = true }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutable.value = UiState.Error(e.userMessage()) }
        }
    }
}

