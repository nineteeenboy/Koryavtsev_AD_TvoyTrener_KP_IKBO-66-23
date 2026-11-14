package com.example.trainer

import android.graphics.Bitmap
import android.content.ContentValues
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.trainer.data.remote.dto.*
import com.example.trainer.domain.model.*
import com.example.trainer.presentation.application.*
import com.example.trainer.presentation.profile.ProfileScreen
import com.example.trainer.presentation.theme.TrainerTheme
import com.example.trainer.presentation.trainers.*
import com.example.trainer.util.UiState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.time.Instant
import java.time.LocalDate
import com.example.trainer.util.gymZone
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.runBlocking

/** Screen contracts and anonymized screenshots rendered by the actual Compose components. */
class TrainerWorkspaceScreensTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val application = TrainerApplicationDto(101, "candidate", "Александр Иванов", "trainer@example.com",
        "+7 (999) 123-45-67", "Силовые тренировки", 5, 2500.0, "Персональные тренировки, постановка техники, работа со свободными весами.",
        "Высшее физкультурное образование", "Персональный тренер", "PENDING", "2026-11-12T12:00:00Z")
    private fun trainerFixture(): Trainer {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val portrait = File(instrumentation.targetContext.cacheDir, "report-trainer.jpg")
        instrumentation.context.assets.open("report-trainer.jpg").use { source -> portrait.outputStream().use { source.copyTo(it) } }
        runBlocking {
            check(instrumentation.targetContext.imageLoader.execute(ImageRequest.Builder(instrumentation.targetContext)
                .data(portrait).build()) is SuccessResult)
        }
        return Trainer(1, "Инструктор тренажёрного зала", "Силовые тренировки", portrait.toURI().toString(),
            "Опыт работы\nПерсональные занятия и постановка техники.", 8, 2500.0, 4.8, 12)
    }
    private fun screenshot(name: String) {
        compose.waitForIdle()
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val metadata = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$name.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TrainerReport")
        }
        val uri = checkNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, metadata))
        checkNotNull(resolver.openOutputStream(uri)).use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
    @Test fun formRetainsInputOnServerErrorAndShowsActualSavingState() {
        compose.setContent { TrainerTheme {
            TrainerApplicationScreen({}, {}, UiState.Error("Не удалось сохранить заявку. Повторите попытку."),
                TrainerApplicationRequest(application.email, application.phone, application.experience, application.education,
                    application.certificates, application.specialization, 5, 2500.0))
        } }
        screenshot("application")
        compose.onNodeWithTag("submitTrainerApplication").performScrollTo()
        compose.onNodeWithTag("error").assertTextContains("Не удалось сохранить", substring = true)
        compose.onNodeWithTag("applicationEmail").performScrollTo().assertTextContains(application.email)
    }
    @Test fun pendingStatusDisplaysPersistedApplication() {
        compose.setContent { TrainerTheme { ApplicationStatusScreen(application, {}, {}, {}, {}) } }
        compose.onNodeWithTag("applicationStatus").assertTextEquals("Заявка на рассмотрении")
        screenshot("pending")
    }
    @Test fun adminRejectRequiresReasonAndSendsDecision() {
        var result: Triple<Int, Boolean, String>? = null
        compose.setContent { TrainerTheme {
            AdminApplicationsScreen(UiState.Success(listOf(application)), UiState.Empty,
                { id, approve, comment -> result = Triple(id, approve, comment) }, {}, {}, {})
        } }
        screenshot("admin")
        compose.onNodeWithText("Отклонить").performScrollTo().performClick()
        compose.onNodeWithText("Подтвердить решение").assertIsNotEnabled()
        compose.onNodeWithText("Причина отказа").performTextInput("Уточните образование")
        compose.onNodeWithText("Подтвердить решение").performClick()
        compose.runOnIdle { assertEquals(Triple(101, false, "Уточните образование"), result) }
    }
    @Test fun diaryEditsNoteAndShowsServerConfirmation() {
        val upcoming = LocalDate.now(gymZone).plusDays(1).atTime(10, 0).atZone(gymZone).toInstant().toString()
        var saved: String? = null
        compose.setContent {
            var sessions by remember { mutableStateOf(listOf(TrainingSessionDto(301, "Михаил Петров", upcoming,
                "ACTIVE", 2500.0, "Разминка 10 минут. Приседания: 3 × 10, 40 кг."))) }
            var action by remember { mutableStateOf<UiState<Unit>>(UiState.Empty) }
            TrainerTheme { TrainingDiaryScreen(UiState.Success(sessions), action,
                { id, note -> saved = note; sessions = sessions.map { if (it.id == id) it.copy(note = note) else it }; action = UiState.Success(Unit) },
                { action = UiState.Empty }, {}, {}) }
        }
        screenshot("diary")
        compose.onNodeWithText("Редактировать заметку").performClick()
        compose.onNodeWithTag("trainingNote").performTextReplacement("Жим лёжа: 3 × 8, 50 кг.")
        screenshot("note")
        compose.onNodeWithTag("saveTrainingNote").performClick()
        compose.onNodeWithTag("trainingNote").assertDoesNotExist()
        compose.onNodeWithText("Жим лёжа: 3 × 8, 50 кг.").assertExists()
        compose.runOnIdle { assertEquals("Жим лёжа: 3 × 8, 50 кг.", saved) }
    }
    @Test fun trainerProfileOffersDiaryAndAdminProfileOffersModeration() {
        compose.setContent { TrainerTheme {
            ProfileScreen(UiState.Success(User("trainer", "trainer@example.com", "Александр Иванов", "TRAINER", 18)),
                UiState.Empty, false, {}, {}, {}, {})
        } }
        compose.onNodeWithText("Дневник тренировок").assertExists()
        compose.onNodeWithText("Заявки тренеров").assertDoesNotExist()
        screenshot("profile")
    }
    @Test fun catalogShowsPricesWithoutEducationalLabels() {
        val trainer = trainerFixture()
        compose.setContent { TrainerTheme { TrainerListScreen(UiState.Success(listOf(trainer)), {}, {}, {}, {}) } }
        compose.onNodeWithText("2500 ₽ / час").assertExists()
        screenshot("catalog")
    }
    @Test fun trainerDetailsShowPriceWithoutEducationalLabels() {
        val trainer = trainerFixture()
        compose.setContent { TrainerTheme { TrainerDetailScreen(UiState.Success(trainer), UiState.Success(emptyList()), {}, {}, {}, {}) } }
        screenshot("detail")
        compose.onNodeWithText("2500 ₽ / час").performScrollTo().assertExists()
    }
}
