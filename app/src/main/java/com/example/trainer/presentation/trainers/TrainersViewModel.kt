package com.example.trainer.presentation.trainers

import androidx.lifecycle.*
import com.example.trainer.domain.model.*
import com.example.trainer.domain.usecase.*
import com.example.trainer.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/** Loads catalog and trainer details through domain operations. */
@HiltViewModel
class TrainersViewModel @Inject constructor(
    private val getTrainers: GetTrainersUseCase,
    private val getTrainer: GetTrainerByIdUseCase,
    private val getReviews: GetReviewsUseCase,
) : ViewModel() {
    private val list = MutableStateFlow<UiState<List<Trainer>>>(UiState.Loading)
    val state = list.asStateFlow()
    private val detail = MutableStateFlow<UiState<Trainer>>(UiState.Loading)
    val detailState = detail.asStateFlow()
    private val reviews = MutableStateFlow<UiState<List<Review>>>(UiState.Loading)
    val reviewsState = reviews.asStateFlow()
    private var listJob: Job? = null
    private var detailJob: Job? = null
    fun load(filter: TrainerFilter = TrainerFilter()) {
        listJob?.cancel()
        listJob = viewModelScope.launch {
            list.value = UiState.Loading
            try {
                val result = getTrainers(filter)
                list.value = if (result.isEmpty()) UiState.Empty else UiState.Success(result)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { list.value = UiState.Error(e.userMessage()) }
        }
    }
    fun loadDetail(id: Int) {
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            detail.value = UiState.Loading
            reviews.value = UiState.Loading
            try { detail.value = UiState.Success(getTrainer(id)) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { detail.value = UiState.Error(e.userMessage()) }
            try {
                val result = getReviews(id)
                reviews.value = if (result.isEmpty()) UiState.Empty else UiState.Success(result)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { reviews.value = UiState.Error(e.userMessage()) }
        }
    }
}

