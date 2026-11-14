package com.example.trainer

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.trainer.presentation.application.TrainerApplicationPendingScreen
import com.example.trainer.presentation.application.TrainerApplicationScreen
import com.example.trainer.presentation.theme.TrainerTheme
import com.example.trainer.presentation.trainers.TrainerListScreen
import com.example.trainer.util.UiState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Verifies the trainer application entry point, validation and confirmation actions. */
class TrainerApplicationTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun catalogOpensTrainerApplication() {
        var opened = false
        compose.setContent { TrainerTheme {
            TrainerListScreen(UiState.Empty, {}, {}, {}, {}, onBecomeTrainer = { opened = true })
        } }
        compose.onNodeWithTag("becomeTrainer").performClick()
        compose.runOnIdle { assertTrue(opened) }
    }

    @Test fun applicationRequiresValidContactsAndAllowsMissingCertificates() {
        var returnedHome = false
        compose.setContent {
            var submitted by remember { mutableStateOf(false) }
            TrainerTheme {
                if (submitted) TrainerApplicationPendingScreen { returnedHome = true }
                else TrainerApplicationScreen({ submitted = true }, {})
            }
        }
        compose.onNodeWithTag("submitTrainerApplication").performScrollTo().performClick()
        compose.onNodeWithTag("applicationPending").assertDoesNotExist()
        compose.onNodeWithTag("applicationEmail").performScrollTo().performTextInput("trainer@example.com")
        compose.onNodeWithTag("applicationSpecialization").performScrollTo().performTextInput("Силовая подготовка")
        compose.onNodeWithTag("applicationYears").performScrollTo().performTextInput("5")
        compose.onNodeWithTag("applicationPrice").performScrollTo().performTextInput("2500")
        compose.onNodeWithTag("applicationPhone").performScrollTo().performTextInput("123")
        compose.onNodeWithTag("applicationExperience").performScrollTo().performTextInput("5 лет в фитнес-клубе")
        compose.onNodeWithTag("applicationEducation").performScrollTo().performTextInput("Университет физической культуры")
        compose.onNodeWithTag("submitTrainerApplication").performScrollTo().performClick()
        compose.onNodeWithTag("applicationPending").assertDoesNotExist()
        compose.onNodeWithTag("applicationPhone").performScrollTo().performTextReplacement("+7 (999) 123-45-67")
        compose.onNodeWithTag("submitTrainerApplication").performScrollTo().performClick()
        compose.onNodeWithTag("applicationPending").assertTextEquals("Заявка на рассмотрении")
        compose.onNodeWithTag("applicationHome").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(returnedHome) }
    }
}
