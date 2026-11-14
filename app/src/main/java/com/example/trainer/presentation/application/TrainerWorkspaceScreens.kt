package com.example.trainer.presentation.application

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.trainer.data.remote.dto.*
import com.example.trainer.presentation.components.*
import com.example.trainer.util.*
import java.time.*

fun applicationStatus(status: String) = when (status) {
    "APPROVED" -> "Заявка одобрена"
    "REJECTED" -> "Заявка отклонена"
    else -> "Заявка на рассмотрении"
}

@Composable
fun ApplicationStatusScreen(application: TrainerApplicationDto, onEdit: () -> Unit,
    onDiary: () -> Unit, onRefresh: () -> Unit, onBack: () -> Unit) {
    ScreenFrame("Моя заявка", onBack) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text(applicationStatus(application.status), style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.testTag("applicationStatus")) }
            item { Text("Отправлена ${displayInstant(application.submittedAt)}") }
            item { Text(when (application.status) {
                "APPROVED" -> "Профиль опубликован в каталоге. Вам доступен дневник записей и заметки к тренировкам."
                "REJECTED" -> "Исправьте заявку с учётом комментария администратора и отправьте её повторно."
                else -> "Заявка сохранена. После решения администратора её статус появится здесь."
            }) }
            if (application.adminComment.isNotBlank()) item {
                Text("Комментарий администратора", style = MaterialTheme.typography.titleMedium)
                Text(application.adminComment)
            }
            item { ApplicationDetails(application) }
            if (application.status == "REJECTED") item {
                Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) { Text("Исправить и отправить повторно") }
            }
            if (application.status == "APPROVED") item {
                Button(onClick = onDiary, modifier = Modifier.fillMaxWidth()) { Text("Дневник тренировок") }
            }
            item { OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) { Text("Обновить статус") } }
        }
    }
}

@Composable
private fun ApplicationDetails(application: TrainerApplicationDto) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(application.name, style = MaterialTheme.typography.titleLarge)
        Text("${application.specialization} · стаж ${application.experienceYears} лет")
        Text("${application.price.toInt()} ₽ / час")
        Text("${application.email}\n${application.phone}")
        Text("Опыт работы", style = MaterialTheme.typography.titleMedium); Text(application.experience)
        Text("Образование", style = MaterialTheme.typography.titleMedium); Text(application.education)
        if (application.certificates.isNotBlank()) {
            Text("Сертификаты", style = MaterialTheme.typography.titleMedium); Text(application.certificates)
        }
    }
}

@Composable
fun AdminApplicationsScreen(state: UiState<List<TrainerApplicationDto>>, action: UiState<Unit>,
    onDecide: (Int, Boolean, String) -> Unit, onReset: () -> Unit,
    onRefresh: () -> Unit, onBack: () -> Unit) {
    var history by rememberSaveable { mutableStateOf(false) }
    var selected by remember { mutableStateOf<TrainerApplicationDto?>(null) }
    var approve by remember { mutableStateOf(true) }
    var comment by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(action) { if (action is UiState.Success) { selected = null; onReset() } }
    ScreenFrame("Заявки тренеров", onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(!history, { history = false }, label = { Text("На рассмотрении") })
                FilterChip(history, { history = true }, label = { Text("История") })
            }
            TextButton(onClick = onRefresh) { Text("Обновить") }
            StateContent(state, onRefresh) { all ->
                val applications = all.filter { (it.status != "PENDING") == history }
                if (applications.isEmpty()) Text("Заявок пока нет", Modifier.padding(24.dp))
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(applications, key = { it.id }) { application ->
                        Card(Modifier.fillMaxWidth().testTag("adminApplication${application.id}")) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(applicationStatus(application.status), style = MaterialTheme.typography.titleMedium)
                                Text(displayInstant(application.submittedAt))
                                ApplicationDetails(application)
                                if (application.adminComment.isNotBlank()) Text("Решение: ${application.adminComment}")
                                if (application.status == "PENDING") Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(onClick = { onReset(); selected = application; approve = true; comment = "" }) { Text("Подтвердить") }
                                    OutlinedButton(onClick = { onReset(); selected = application; approve = false; comment = "" }) { Text("Отклонить") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    selected?.let { application ->
        AlertDialog(onDismissRequest = { if (action !is UiState.Loading) selected = null },
            title = { Text(if (approve) "Подтвердить заявку?" else "Отклонить заявку?") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(application.name)
                Text(if (approve) "Профиль появится в каталоге, аккаунт получит доступ тренера." else "Укажите, что нужно исправить.")
                OutlinedTextField(comment, { if (it.length <= 2000) comment = it },
                    label = { Text(if (approve) "Комментарий (необязательно)" else "Причина отказа") }, minLines = 2)
                ActionStatus(action)
            } }, confirmButton = {
                TextButton(onClick = { onDecide(application.id, approve, comment) },
                    enabled = action !is UiState.Loading && (approve || comment.isNotBlank())) { Text("Подтвердить решение") }
            }, dismissButton = {
                TextButton(onClick = { selected = null }, enabled = action !is UiState.Loading) { Text("Отмена") }
            })
    }
}

@Composable
fun TrainingDiaryScreen(state: UiState<List<TrainingSessionDto>>, action: UiState<Unit>,
    onSave: (Int, String) -> Unit, onReset: () -> Unit, onRefresh: () -> Unit, onBack: () -> Unit) {
    var filter by rememberSaveable { mutableStateOf("Предстоящие") }
    var selected by remember { mutableStateOf<TrainingSessionDto?>(null) }
    var draft by rememberSaveable { mutableStateOf("") }
    var discard by remember { mutableStateOf(false) }
    LaunchedEffect(action) { if (action is UiState.Success) { selected = null; onReset() } }
    fun closeEditor() { if (draft != selected?.note) discard = true else selected = null }
    ScreenFrame("Дневник тренировок", onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Предстоящие", "Прошедшие", "Все").forEach { label ->
                    FilterChip(filter == label, { filter = label }, label = { Text(label) })
                }
            }
            TextButton(onClick = onRefresh) { Text("Обновить записи") }
            StateContent(state, onRefresh) { all ->
                val now = Instant.now()
                val sessions = all.filter {
                    when (filter) {
                        "Предстоящие" -> it.status == "ACTIVE" && Instant.parse(it.startsAt).plusSeconds(3600) > now
                        "Прошедшие" -> it.status == "ACTIVE" && Instant.parse(it.startsAt).plusSeconds(3600) <= now
                        else -> true
                    }
                }.let { if (filter == "Предстоящие") it.sortedBy { session -> session.startsAt } else it }
                if (sessions.isEmpty()) Text("В этом разделе пока нет записей", Modifier.padding(24.dp))
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    sessions.groupBy { Instant.parse(it.startsAt).atZone(gymZone).toLocalDate() }.forEach { (date, day) ->
                        item(key = "date$date") { Text(date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            style = MaterialTheme.typography.titleLarge) }
                        items(day, key = { it.id }) { session ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("${displayTime(session.startsAt)} · ${session.clientName}", style = MaterialTheme.typography.titleMedium)
                                    Text(if (session.status == "CANCELLED") "Отменена" else if (Instant.parse(session.startsAt).plusSeconds(3600) <= now)
                                        "Завершена" else "Запланирована")
                                    Text("${session.price.toInt()} ₽")
                                    Text(session.note.ifBlank { "Заметка пока не добавлена" })
                                    session.noteUpdatedAt?.let { Text("Сохранено ${displayInstant(it)}", style = MaterialTheme.typography.bodySmall) }
                                    OutlinedButton(onClick = { onReset(); selected = session; draft = session.note }) {
                                        Text(if (session.note.isBlank()) "Добавить заметку" else "Редактировать заметку")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    selected?.let { session ->
        AlertDialog(onDismissRequest = { if (action !is UiState.Loading) closeEditor() },
            title = { Text("${session.clientName} · ${displayTime(session.startsAt)}") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Упражнения, подходы, рабочие веса и результаты. Заметка видна только вам.")
                OutlinedTextField(draft, { if (it.length <= 20_000) draft = it },
                    modifier = Modifier.fillMaxWidth().testTag("trainingNote"), minLines = 5, maxLines = 9,
                    label = { Text("Запись о тренировке") }, supportingText = { Text("${draft.length} / 20000") })
                ActionStatus(action)
            } }, confirmButton = {
                TextButton(onClick = { onSave(session.id, draft) }, enabled = action !is UiState.Loading && draft != session.note,
                    modifier = Modifier.testTag("saveTrainingNote")) { Text("Сохранить") }
            }, dismissButton = {
                TextButton(onClick = { closeEditor() }, enabled = action !is UiState.Loading) { Text("Закрыть") }
            })
    }
    if (discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text("Закрыть без сохранения?") },
        text = { Text("Изменения заметки ещё не сохранены.") },
        confirmButton = { TextButton(onClick = { discard = false; selected = null }) { Text("Не сохранять") } },
        dismissButton = { TextButton(onClick = { discard = false }) { Text("Продолжить редактирование") } })
}
