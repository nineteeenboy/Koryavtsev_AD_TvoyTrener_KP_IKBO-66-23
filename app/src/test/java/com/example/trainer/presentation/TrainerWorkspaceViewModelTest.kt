package com.example.trainer.presentation

import com.example.trainer.data.remote.api.TrainerApi
import com.example.trainer.data.remote.dto.*
import com.example.trainer.presentation.application.TrainerWorkspaceViewModel
import com.example.trainer.util.UiState
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrainerWorkspaceViewModelTest {
    @get:Rule val dispatcher = MainDispatcherRule()
    private val api = mockk<TrainerApi>()
    private val application = TrainerApplicationDto(1, "u", "Тренер", "a@b.ru", "+79991234567",
        "Фитнес", 3, 2000.0, "Опыт", "Образование", "", "PENDING", "2026-11-12T10:00:00Z")
    private val request = TrainerApplicationRequest("a@b.ru", "+79991234567", "Опыт", "Образование", price = 2000.0)

    @Test fun submitShowsSuccessOnlyAfterServerPersistsApplication() = runTest {
        coEvery { api.submitApplication(request) } coAnswers { kotlinx.coroutines.delay(100); application }
        val vm = TrainerWorkspaceViewModel(api)
        vm.submit(request)
        assertTrue(vm.action.value is UiState.Loading)
        advanceUntilIdle()
        assertTrue(vm.action.value is UiState.Success)
        assertEquals(application, (vm.applications.value as UiState.Success).data.single())
    }
    @Test fun failedSubmissionCanBeRetriedWithoutFalseConfirmation() = runTest {
        coEvery { api.submitApplication(request) } throws IllegalStateException("Нет соединения")
        val vm = TrainerWorkspaceViewModel(api)
        vm.submit(request); advanceUntilIdle()
        assertTrue(vm.action.value is UiState.Error)
        coEvery { api.submitApplication(request) } returns application
        vm.submit(request); advanceUntilIdle()
        assertTrue(vm.action.value is UiState.Success)
    }
    @Test fun doubleTapSendsOneApplication() = runTest {
        coEvery { api.submitApplication(request) } coAnswers { kotlinx.coroutines.delay(100); application }
        val vm = TrainerWorkspaceViewModel(api)
        vm.submit(request); vm.submit(request); advanceUntilIdle()
        coVerify(exactly = 1) { api.submitApplication(request) }
    }
    @Test fun adminDecisionUpdatesApplicationStatus() = runTest {
        coEvery { api.applications() } returns listOf(application)
        coEvery { api.decideApplication(1, false, "Уточните образование") } returns
            application.copy(status = "REJECTED", adminComment = "Уточните образование")
        val vm = TrainerWorkspaceViewModel(api)
        vm.loadApplications(true); advanceUntilIdle()
        vm.decide(1, false, "Уточните образование"); advanceUntilIdle()
        assertEquals("REJECTED", (vm.applications.value as UiState.Success).data.single().status)
    }
    @Test fun diarySavesAndUpdatesNoteFromServer() = runTest {
        val session = TrainingSessionDto(1, "Клиент", "2026-11-13T07:00:00Z", "ACTIVE", 2000.0)
        coEvery { api.trainingSessions() } returns listOf(session)
        coEvery { api.saveTrainingNote(1, "Присед 3 × 10") } returns session.copy(note = "Присед 3 × 10")
        val vm = TrainerWorkspaceViewModel(api)
        vm.loadSessions(); advanceUntilIdle()
        vm.saveNote(1, "Присед 3 × 10"); advanceUntilIdle()
        assertEquals("Присед 3 × 10", (vm.sessions.value as UiState.Success).data.single().note)
    }
    @Test fun failedNoteSavePreservesPreviouslyLoadedDiary() = runTest {
        val session = TrainingSessionDto(1, "Клиент", "2026-11-13T07:00:00Z", "ACTIVE", 2000.0, "Старая заметка")
        coEvery { api.trainingSessions() } returns listOf(session)
        coEvery { api.saveTrainingNote(any(), any()) } throws IllegalStateException("Ошибка сервера")
        val vm = TrainerWorkspaceViewModel(api)
        vm.loadSessions(); advanceUntilIdle()
        vm.saveNote(1, "Новая заметка"); advanceUntilIdle()
        assertTrue(vm.action.value is UiState.Error)
        assertEquals("Старая заметка", (vm.sessions.value as UiState.Success).data.single().note)
    }
}
