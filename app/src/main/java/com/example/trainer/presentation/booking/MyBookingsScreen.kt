package com.example.trainer.presentation.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trainer.domain.model.Booking
import com.example.trainer.presentation.components.*
import com.example.trainer.util.*
import java.time.Instant

/** Personal reservations with explicit cancellation confirmation. */
@Composable
fun MyBookingsScreen(
    state: UiState<List<Booking>>, cancelState: UiState<Unit>,
    onRetry: () -> Unit, onCancel: (Int) -> Unit, onReview: (Int) -> Unit, onBack: () -> Unit,
) {
    var cancelId by remember { mutableStateOf<Int?>(null) }
    ScreenFrame("Мои записи", onBack) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ActionStatus(cancelState)
            TextButton(onClick = onRetry) { Text("Обновить") }
            StateContent(state, onRetry, "Вы ещё не записались на тренировку") { bookings ->
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(bookings, key = { it.id }) { booking ->
                        val start = Instant.parse(booking.startsAt)
                        val upcoming = start > Instant.now()
                        val completed = start.plusSeconds(3600) <= Instant.now()
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(booking.trainerName, style = MaterialTheme.typography.titleLarge)
                                Text(displayInstant(booking.startsAt))
                                Text("${booking.price.toInt()} ₽")
                                Text(when {
                                    booking.status == "CANCELLED" -> "Отменена"
                                    completed -> "Завершена"
                                    !upcoming -> "Идёт тренировка"
                                    else -> "Предстоит"
                                })
                                if (booking.status == "ACTIVE" && upcoming) OutlinedButton(
                                    onClick = { cancelId = booking.id }, enabled = cancelState != UiState.Loading,
                                ) { Text("Отменить запись") }
                                if (booking.status == "ACTIVE" && completed) TextButton(onClick = { onReview(booking.trainerId) }) {
                                    Text("Оставить отзыв")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    cancelId?.let { id ->
        AlertDialog(onDismissRequest = { cancelId = null }, title = { Text("Отменить тренировку?") },
            confirmButton = { TextButton(onClick = { onCancel(id); cancelId = null }) { Text("Отменить запись") } },
            dismissButton = { TextButton(onClick = { cancelId = null }) { Text("Оставить запись") } })
    }
}

