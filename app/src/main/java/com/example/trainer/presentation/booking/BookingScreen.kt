package com.example.trainer.presentation.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.trainer.domain.model.Booking
import com.example.trainer.presentation.components.*
import com.example.trainer.util.*
import java.time.*

/** DatePicker and available-time picker select server-issued slots only. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    slots: UiState<List<String>>, action: UiState<Booking>,
    onDate: (String) -> Unit, onBook: (String) -> Unit, onBack: () -> Unit,
) {
    var date by rememberSaveable { mutableStateOf(LocalDate.now(gymZone).toString()) }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var dateDialog by remember { mutableStateOf(false) }
    var timeDialog by remember { mutableStateOf(false) }
    val busy = action == UiState.Loading
    LaunchedEffect(date) { selected = null; onDate(date) }
    LaunchedEffect(slots) {
        if (slots is UiState.Success && selected !in slots.data) selected = null
    }
    ScreenFrame("Запись на тренировку", onBack) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Занятие длится 1 час. Время указано для зала: МСК.")
            OutlinedButton(onClick = { dateDialog = true }, enabled = !busy, modifier = Modifier.fillMaxWidth().testTag("pickDate")) {
                Text("Дата: $date")
            }
            StateContent(slots, { onDate(date) }, "На эту дату свободного времени нет") { list ->
                OutlinedButton(onClick = { timeDialog = true }, enabled = !busy, modifier = Modifier.fillMaxWidth().testTag("pickTime")) {
                    Text(selected?.let { "Время: ${displayTime(it)}" } ?: "Выбрать время")
                }
                if (timeDialog) AlertDialog(
                    onDismissRequest = { timeDialog = false },
                    title = { Text("Свободное время, МСК") },
                    text = {
                        Column(Modifier.heightIn(max = 350.dp).verticalScroll(rememberScrollState())) {
                            list.forEach { instant ->
                                TextButton(onClick = { selected = instant; timeDialog = false },
                                    modifier = Modifier.fillMaxWidth().testTag("slot_${displayTime(instant)}")) {
                                    Text(displayTime(instant))
                                }
                            }
                        }
                    },
                    confirmButton = { TextButton(onClick = { timeDialog = false }) { Text("Закрыть") } },
                )
            }
            ActionStatus(action)
            Button(onClick = { selected?.let(onBook) }, enabled = selected != null && !busy,
                modifier = Modifier.fillMaxWidth().testTag("confirmBooking")) { Text("Подтвердить запись") }
        }
    }
    if (dateDialog) {
        val today = LocalDate.now(gymZone)
        val picker = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val candidate = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                    return candidate >= today && candidate <= today.plusDays(90)
                }
                override fun isSelectableYear(year: Int) = year in today.year..today.plusDays(90).year
            },
        )
        DatePickerDialog(onDismissRequest = { dateDialog = false }, confirmButton = {
            TextButton(onClick = {
                picker.selectedDateMillis?.let {
                    date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()
                }
                dateDialog = false
            }) { Text("Выбрать") }
        }, dismissButton = { TextButton(onClick = { dateDialog = false }) { Text("Отмена") } }) { DatePicker(picker) }
    }
}

