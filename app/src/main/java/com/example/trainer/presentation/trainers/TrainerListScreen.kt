package com.example.trainer.presentation.trainers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.trainer.domain.model.*
import com.example.trainer.presentation.components.*
import com.example.trainer.util.UiState
import com.example.trainer.R

/** Catalog with server-side specialization, maximum price and minimum rating filters. */
@Composable
fun TrainerListScreen(
    state: UiState<List<Trainer>>, onFilter: (TrainerFilter) -> Unit,
    onTrainer: (Int) -> Unit, onBookings: () -> Unit, onProfile: () -> Unit,
    onBecomeTrainer: () -> Unit = {},
) {
    var specialization by rememberSaveable { mutableStateOf("") }
    var maxPrice by rememberSaveable { mutableStateOf("") }
    var minRating by rememberSaveable { mutableStateOf(0f) }
    var filterError by remember { mutableStateOf<String?>(null) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    fun applyFilter() {
        val price = maxPrice.takeIf { it.isNotBlank() }?.replace(',', '.')?.toDoubleOrNull()
        if (maxPrice.isNotBlank() && (price == null || !price.isFinite() || price < 0)) {
            filterError = "Введите неотрицательную цену"
        } else {
            filterError = null
            onFilter(TrainerFilter(specialization.trim().takeIf { it.isNotBlank() }, price, minRating.toDouble()))
        }
    }
    ScreenFrame("Тренеры") { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onBookings) { Icon(Icons.Default.Event, null); Text("Мои записи") }
                TextButton(onClick = onProfile) { Icon(Icons.Default.Person, null); Text("Профиль") }
            }
            Text(stringResource(R.string.trainer_catalog_intro), Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyLarge)
            OutlinedButton(onClick = onBecomeTrainer,
                modifier = Modifier.padding(horizontal = 16.dp).testTag("becomeTrainer")) {
                Icon(Icons.Default.FitnessCenter, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.become_trainer))
            }
            TextButton(onClick = { showFilters = !showFilters }, modifier = Modifier.padding(horizontal = 8.dp)) {
                Icon(Icons.Default.Tune, null)
                Text(stringResource(if (showFilters) R.string.trainer_filters_hide else R.string.trainer_filters))
            }
            if (showFilters) Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(specialization, { specialization = it }, singleLine = true,
                    label = { Text("Специализация") }, supportingText = { Text("Например: Йога или Бокс") },
                    modifier = Modifier.fillMaxWidth().testTag("specialization"))
                OutlinedTextField(maxPrice, { maxPrice = it }, singleLine = true,
                    label = { Text("Максимальная цена, ₽") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("maxPrice"))
                Text("Рейтинг от ${"%.1f".format(minRating)}")
                Slider(minRating, { minRating = it }, valueRange = 0f..5f, steps = 9)
                filterError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { applyFilter() }, modifier = Modifier.testTag("applyFilter")) { Text("Применить") }
                    TextButton(onClick = {
                        specialization = ""; maxPrice = ""; minRating = 0f
                        filterError = null; onFilter(TrainerFilter())
                    }) { Text("Сбросить") }
                }
            }
            StateContent(state, onRetry = { applyFilter() }, emptyText = "Тренеры по этим фильтрам не найдены") { trainers ->
                LazyColumn(Modifier.fillMaxSize().testTag("trainerList"), contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(trainers, key = { it.id }) { trainer ->
                        TrainerCard(trainer, { onTrainer(trainer.id) })
                    }
                }
            }
        }
    }
}

