package com.example.trainer

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.trainer.domain.model.*
import com.example.trainer.presentation.auth.LoginScreen
import com.example.trainer.presentation.booking.BookingScreen
import com.example.trainer.presentation.trainers.TrainerListScreen
import com.example.trainer.presentation.theme.TrainerTheme
import com.example.trainer.presentation.landing.LandingScreen
import com.example.trainer.presentation.trainers.TrainerDetailScreen
import com.example.trainer.util.UiState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Compose UI contracts without a live Firebase project or database. */
class KeyScreensTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun loginRequiresInputAndSendsCredentials() {
        var credentials: Pair<String, String>? = null
        compose.setContent { TrainerTheme { LoginScreen(UiState.Empty,
            { email, password -> credentials = email to password }, {}) } }
        compose.onNodeWithTag("login").assertIsNotEnabled()
        compose.onNodeWithTag("email").performTextInput("a@b.ru")
        compose.onNodeWithTag("password").performTextInput("secret")
        compose.onNodeWithTag("login").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("a@b.ru" to "secret", credentials) }
    }
    @Test fun loginDisplaysBackendError() {
        compose.setContent { TrainerTheme { LoginScreen(UiState.Error("Проверьте пароль"), { _, _ -> }, {}) } }
        compose.onNodeWithTag("error").assertTextEquals("Проверьте пароль")
    }
    @Test fun trainerCardOpensCorrectId() {
        var selected: Int? = null
        val trainer = Trainer(7, "Иван Орлов", "Бокс", "", "Описание", 8, 2000.0, 4.5, 2)
        compose.setContent { TrainerTheme {
            TrainerListScreen(UiState.Success(listOf(trainer)), {}, { selected = it }, {}, {})
        } }
        compose.onNodeWithTag("trainerList").performScrollToNode(hasTestTag("trainer_7"))
        compose.onNodeWithTag("trainer_7").performClick()
        compose.runOnIdle { assertEquals(7, selected) }
    }
    @Test fun catalogShowsEmptyState() {
        compose.setContent { TrainerTheme { TrainerListScreen(UiState.Empty, {}, {}, {}, {}) } }
        compose.onNodeWithTag("empty").assertTextEquals("Тренеры по этим фильтрам не найдены")
    }
    @Test fun bookingRequiresSlotSelectionAndSendsUtcInstant() {
        val instant = "2030-01-01T06:00:00Z"
        var reserved: String? = null
        compose.setContent { TrainerTheme {
            BookingScreen(UiState.Success(listOf(instant)), UiState.Empty, {}, { reserved = it }, {})
        } }
        compose.onNodeWithTag("confirmBooking").assertIsNotEnabled()
        compose.onNodeWithTag("pickTime").performClick()
        compose.onNodeWithTag("slot_09:00").performClick()
        compose.onNodeWithTag("confirmBooking").performClick()
        compose.runOnIdle { assertEquals(instant, reserved) }
    }
    @Test fun bookingDisplaysOccupiedSlotError() {
        compose.setContent { TrainerTheme {
            BookingScreen(UiState.Empty, UiState.Error("Это время уже занято"), {}, {}, {})
        } }
        compose.onNodeWithTag("error").assertTextEquals("Это время уже занято")
    }
    @Test fun welcomeEndsWithWorkingSignInAction() {
        var continued = false
        compose.setContent { TrainerTheme {
            LandingScreen(UiState.Empty, false, { continued = true }, {}, {})
        } }
        compose.onNodeWithTag("landingList").performScrollToNode(hasTestTag("landingContinue"))
        compose.onNodeWithTag("landingContinue").assertTextContains("Зарегистрироваться / войти").performClick()
        compose.runOnIdle { assertTrue(continued) }
    }
    @Test fun welcomeOpensSuppliedTrainerProfile() {
        var selected: Int? = null
        val trainer = Trainer(13, "Инструктор тренажёрного зала", "Силовые тренировки", "",
            "Образование и достижения\nЧемпион Москвы", 8, 2500.0, 0.0, 0, true)
        compose.setContent { TrainerTheme {
            LandingScreen(UiState.Success(listOf(trainer)), false, {}, { selected = it }, {})
        } }
        compose.onNodeWithTag("landingList").performScrollToNode(hasTestTag("trainer_13"))
        compose.onNodeWithTag("trainer_13").performClick()
        compose.runOnIdle { assertEquals(13, selected) }
    }
    @Test fun profileDisplaysAchievementsAndBookingAction() {
        var booked = false
        val trainer = Trainer(13, "Инструктор тренажёрного зала", "Силовые тренировки", "",
            "Образование и достижения\nЧемпион Москвы", 8, 2500.0, 0.0, 0, true)
        compose.setContent { TrainerTheme {
            TrainerDetailScreen(UiState.Success(trainer), UiState.Empty, {}, { booked = true }, {}, {})
        } }
        compose.onNodeWithText("Чемпион Москвы").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("detailBook").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(booked) }
    }
}

