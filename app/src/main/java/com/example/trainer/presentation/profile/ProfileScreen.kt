package com.example.trainer.presentation.profile

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.trainer.R
import com.example.trainer.domain.model.*
import com.example.trainer.presentation.components.*
import com.example.trainer.util.UiState

/** Verified user profile, appearance control and personal feedback history. */
@Composable
fun ProfileScreen(
    state: UiState<User>, reviews: UiState<List<Review>>, dark: Boolean?,
    onDark: (Boolean) -> Unit, onRetry: () -> Unit, onLogout: () -> Unit, onBack: () -> Unit,
    onBecomeTrainer: () -> Unit = {},
    onAdmin: () -> Unit = {}, onDiary: () -> Unit = {},
) {
    ScreenFrame("Профиль", onBack) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StateContent(state, onRetry, "Войдите в аккаунт") { user ->
                Text(user.name, style = MaterialTheme.typography.headlineMedium)
                Text(user.email)
                Text(when (user.role) { "ADMIN" -> "Администратор"; "TRAINER" -> "Тренер"; else -> "Клиент" })
                if (user.role == "ADMIN") Button(onClick = onAdmin, modifier = Modifier.fillMaxWidth()) {
                    Text("Заявки тренеров")
                }
                if (user.trainerId != null) Button(onClick = onDiary, modifier = Modifier.fillMaxWidth()) {
                    Text("Дневник тренировок")
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Тёмная тема")
                Switch(checked = dark ?: isSystemInDarkTheme(), onCheckedChange = onDark)
            }
            Button(onClick = onBecomeTrainer, modifier = Modifier.fillMaxWidth()) {
                Text("Моя заявка тренера")
            }
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Выйти из аккаунта") }
            Text("Мои отзывы", style = MaterialTheme.typography.titleLarge)
            StateContent(reviews, onRetry, "Вы ещё не оставляли отзывы") { list ->
                list.forEach { review ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Тренер №${review.trainerId} · ${review.rating}/5")
                            Text(review.comment)
                        }
                    }
                }
            }
        }
    }
}

