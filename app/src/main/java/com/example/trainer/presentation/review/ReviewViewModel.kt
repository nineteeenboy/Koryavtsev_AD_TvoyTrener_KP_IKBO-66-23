package com.example.trainer.presentation.review

import androidx.lifecycle.*
import com.example.trainer.domain.model.Review
import com.example.trainer.domain.usecase.LeaveReviewUseCase
import com.example.trainer.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/** Publishes validated feedback without issuing duplicate in-flight requests. */
@HiltViewModel
class ReviewViewModel @Inject constructor(private val leave: LeaveReviewUseCase) : ViewModel() {
    private val mutable = MutableStateFlow<UiState<Review>>(UiState.Empty)
    val state = mutable.asStateFlow()
    fun submit(id: Int, rating: Int, comment: String) {
        if (mutable.value == UiState.Loading) return
        viewModelScope.launch {
            mutable.value = UiState.Loading
            try { mutable.value = UiState.Success(leave(id, rating, comment)) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutable.value = UiState.Error(e.userMessage()) }
        }
    }
}

