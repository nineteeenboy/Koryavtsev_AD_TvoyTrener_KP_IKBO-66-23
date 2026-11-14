package com.example.trainer.presentation.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.trainer.domain.model.Review
import com.example.trainer.presentation.components.*
import com.example.trainer.util.UiState

/** Feedback form available after a completed training; server enforces eligibility. */
@Composable
fun ReviewScreen(state: UiState<Review>, onSubmit: (Int, String) -> Unit, onBack: () -> Unit) {
    var rating by rememberSaveable { mutableStateOf(5f) }
    var comment by rememberSaveable { mutableStateOf("") }
    val busy = state == UiState.Loading
    ScreenFrame("Оставить отзыв", onBack) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Отзыв можно оставить после завершённой тренировки. Один отзыв на тренера.")
            Text("Оценка: ${rating.toInt()} / 5", style = MaterialTheme.typography.titleLarge)
            Slider(rating, { rating = it }, valueRange = 1f..5f, steps = 3, enabled = !busy)
            OutlinedTextField(comment, { if (it.length <= 2000) comment = it },
                label = { Text("Ваш отзыв") }, minLines = 4, enabled = !busy,
                supportingText = { Text("${comment.length}/2000 · минимум 3 символа") },
                modifier = Modifier.fillMaxWidth().testTag("reviewComment"))
            ActionStatus(state)
            Button(onClick = { onSubmit(rating.toInt(), comment) }, enabled = !busy && comment.trim().length >= 3,
                modifier = Modifier.fillMaxWidth()) { Text("Опубликовать") }
        }
    }
}

