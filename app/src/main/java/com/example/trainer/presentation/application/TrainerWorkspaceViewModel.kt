package com.example.trainer.presentation.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainer.data.remote.api.TrainerApi
import com.example.trainer.data.remote.dto.*
import com.example.trainer.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/** Server-backed application, moderation and private diary state. */
@HiltViewModel
class TrainerWorkspaceViewModel @Inject constructor(private val api: TrainerApi) : ViewModel() {
    private val applicationState = MutableStateFlow<UiState<List<TrainerApplicationDto>>>(UiState.Loading)
    val applications = applicationState.asStateFlow()
    private val sessionState = MutableStateFlow<UiState<List<TrainingSessionDto>>>(UiState.Loading)
    val sessions = sessionState.asStateFlow()
    private val actionState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val action = actionState.asStateFlow()

    private fun launchAction(block: suspend () -> Unit) {
        if (actionState.value is UiState.Loading) return
        actionState.value = UiState.Loading
        viewModelScope.launch {
            try { block(); actionState.value = UiState.Success(Unit) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { actionState.value = UiState.Error(e.userMessage()) }
        }
    }
    fun loadApplications(admin: Boolean = false) {
        applicationState.value = UiState.Loading
        viewModelScope.launch {
            try { applicationState.value = UiState.Success(if (admin) api.applications() else api.myApplications()) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { applicationState.value = UiState.Error(e.userMessage()) }
        }
    }
    fun submit(request: TrainerApplicationRequest) = launchAction {
        val saved = api.submitApplication(request)
        applicationState.value = UiState.Success(listOf(saved))
    }
    fun decide(id: Int, approve: Boolean, comment: String) = launchAction {
        val saved = api.decideApplication(id, approve, comment)
        val previous = (applicationState.value as? UiState.Success)?.data.orEmpty()
        applicationState.value = UiState.Success(previous.map { if (it.id == id) saved else it })
    }
    fun loadSessions() {
        sessionState.value = UiState.Loading
        viewModelScope.launch {
            try { sessionState.value = UiState.Success(api.trainingSessions()) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { sessionState.value = UiState.Error(e.userMessage()) }
        }
    }
    fun saveNote(id: Int, note: String) = launchAction {
        val saved = api.saveTrainingNote(id, note)
        val previous = (sessionState.value as? UiState.Success)?.data.orEmpty()
        sessionState.value = UiState.Success(previous.map { if (it.id == id) saved else it })
    }
    fun resetAction() { if (actionState.value !is UiState.Loading) actionState.value = UiState.Empty }
}
