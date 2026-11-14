package com.example.trainer.presentation.booking

import androidx.lifecycle.*
import com.example.trainer.domain.model.Booking
import com.example.trainer.domain.usecase.*
import com.example.trainer.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/** Coordinates available times, reservation creation and cancellation. */
@HiltViewModel
class BookingViewModel @Inject constructor(
    private val book: BookTrainerUseCase,
    private val getBookings: GetMyBookingsUseCase,
    private val cancelBooking: CancelBookingUseCase,
    private val getSlots: GetAvailableSlotsUseCase,
) : ViewModel() {
    private val mutable = MutableStateFlow<UiState<List<Booking>>>(UiState.Loading)
    val state = mutable.asStateFlow()
    private val slotState = MutableStateFlow<UiState<List<String>>>(UiState.Empty)
    val slots = slotState.asStateFlow()
    private val actionState = MutableStateFlow<UiState<Booking>>(UiState.Empty)
    val action = actionState.asStateFlow()
    private val cancellation = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val cancelState = cancellation.asStateFlow()
    private var slotsJob: Job? = null
    private var bookingJob: Job? = null
    fun load() { viewModelScope.launch { loadBookings() } }
    private suspend fun loadBookings() {
        mutable.value = UiState.Loading
        try {
            val result = getBookings()
            mutable.value = if (result.isEmpty()) UiState.Empty else UiState.Success(result)
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { mutable.value = UiState.Error(e.userMessage()) }
    }
    fun loadSlots(id: Int, date: String) {
        slotsJob?.cancel()
        slotsJob = viewModelScope.launch {
            slotState.value = UiState.Loading
            try {
                val result = getSlots(id, date)
                slotState.value = if (result.isEmpty()) UiState.Empty else UiState.Success(result)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { slotState.value = UiState.Error(e.userMessage()) }
        }
    }
    fun reserve(id: Int, instant: String) {
        if (bookingJob?.isActive == true) return
        bookingJob = viewModelScope.launch {
            actionState.value = UiState.Loading
            try { actionState.value = UiState.Success(book(id, instant)) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { actionState.value = UiState.Error(e.userMessage()) }
        }
    }
    fun cancel(id: Int) {
        if (cancellation.value == UiState.Loading) return
        viewModelScope.launch {
            cancellation.value = UiState.Loading
            try {
                cancelBooking(id)
                cancellation.value = UiState.Success(Unit)
                loadBookings()
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { cancellation.value = UiState.Error(e.userMessage()) }
        }
    }
}

