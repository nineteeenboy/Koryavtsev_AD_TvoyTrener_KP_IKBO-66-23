package com.example.trainer.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = FitnessRed, onPrimary = Color.White,
    secondary = FitnessBlack, background = FitnessPaper, surface = Color.White,
    onBackground = FitnessBlack, onSurface = FitnessBlack,
    surfaceContainer = Color(0xFFF4F1F2), primaryContainer = Color(0xFFFFE1E6),
)
private val DarkColors = darkColorScheme(
    primary = FitnessRose, onPrimary = Color(0xFF68001A), secondary = FitnessRose,
    background = FitnessBlack, surface = Color(0xFF191719),
    surfaceContainer = Color(0xFF252125), primaryContainer = Color(0xFF69051C),
)

/** Material 3 theme with persisted optional user override. */
@Composable
fun TrainerTheme(dark: Boolean? = null, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark ?: isSystemInDarkTheme()) DarkColors else LightColors,
        typography = TrainerTypography, content = content,
    )
}

