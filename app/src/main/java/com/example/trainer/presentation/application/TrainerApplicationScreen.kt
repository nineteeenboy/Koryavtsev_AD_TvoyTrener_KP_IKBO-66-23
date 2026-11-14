package com.example.trainer.presentation.application

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.trainer.R
import com.example.trainer.presentation.components.ScreenFrame
import com.example.trainer.presentation.components.ActionStatus
import com.example.trainer.data.remote.dto.TrainerApplicationRequest
import com.example.trainer.util.UiState

/** Validates an application before submitting it to the server. */
@Composable
fun TrainerApplicationScreen(onSubmit: (TrainerApplicationRequest) -> Unit, onBack: () -> Unit,
    action: UiState<Unit> = UiState.Empty, initial: TrainerApplicationRequest? = null) {
    var email by rememberSaveable { mutableStateOf(initial?.email.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(initial?.phone.orEmpty()) }
    var experience by rememberSaveable { mutableStateOf(initial?.experience.orEmpty()) }
    var education by rememberSaveable { mutableStateOf(initial?.education.orEmpty()) }
    var certificates by rememberSaveable { mutableStateOf(initial?.certificates.orEmpty()) }
    var specialization by rememberSaveable { mutableStateOf(initial?.specialization ?: "") }
    var years by rememberSaveable { mutableStateOf(initial?.experienceYears?.toString() ?: "") }
    var price by rememberSaveable { mutableStateOf(initial?.price?.toInt()?.toString() ?: "") }
    var attempted by rememberSaveable { mutableStateOf(false) }
    val emailValid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val phoneValid = phone.count(Char::isDigit) in 10..15 &&
        phone.all { it.isDigit() || it in "+()- " }
    ScreenFrame(stringResource(R.string.trainer_application_title), onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()
            .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.trainer_application_intro), style = MaterialTheme.typography.bodyLarge)
            ApplicationField(specialization, { specialization = it }, R.string.application_specialization,
                "applicationSpecialization", invalid = attempted && specialization.trim().length !in 2..100)
            ApplicationField(years, { years = it }, R.string.application_years,
                "applicationYears", KeyboardType.Number, attempted && years.toIntOrNull() !in 0..80)
            ApplicationField(price, { price = it }, R.string.application_price,
                "applicationPrice", KeyboardType.Decimal,
                attempted && (price.replace(',', '.').toDoubleOrNull()?.let { it.isFinite() && it in 1.0..1_000_000.0 } != true))
            ApplicationField(email, { email = it }, R.string.trainer_application_email, "applicationEmail",
                KeyboardType.Email, attempted && !emailValid, R.string.trainer_application_email_error)
            ApplicationField(phone, { phone = it }, R.string.trainer_application_phone, "applicationPhone",
                KeyboardType.Phone, attempted && !phoneValid, R.string.trainer_application_phone_error)
            ApplicationField(experience, { experience = it }, R.string.trainer_application_experience,
                "applicationExperience", invalid = attempted && experience.isBlank(), multiline = true)
            ApplicationField(education, { education = it }, R.string.trainer_application_education,
                "applicationEducation", invalid = attempted && education.isBlank(), multiline = true)
            ApplicationField(certificates, { certificates = it }, R.string.trainer_application_certificates,
                "applicationCertificates", multiline = true)
            ActionStatus(action)
            Button(onClick = {
                attempted = true
                val rate = price.replace(',', '.').toDoubleOrNull()
                val tenure = years.toIntOrNull()
                if (emailValid && email.length <= 320 && phoneValid && phone.length <= 40 &&
                    experience.trim().length in 1..5000 && education.trim().length in 1..5000 && certificates.length <= 5000 &&
                    specialization.trim().length in 2..100 && tenure != null && tenure in 0..80 &&
                    rate != null && rate.isFinite() && rate in 1.0..1_000_000.0)
                    onSubmit(TrainerApplicationRequest(email, phone, experience, education, certificates,
                        specialization, tenure, rate))
            }, enabled = action !is UiState.Loading,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("submitTrainerApplication")) {
                Text(stringResource(R.string.trainer_application_submit))
            }
        }
    }
}

@Composable
private fun ApplicationField(
    value: String, onValue: (String) -> Unit, label: Int, tag: String,
    keyboard: KeyboardType = KeyboardType.Text, invalid: Boolean = false,
    error: Int = R.string.trainer_application_required, multiline: Boolean = false,
) {
    OutlinedTextField(value, onValue, label = { Text(stringResource(label)) },
        modifier = Modifier.fillMaxWidth().testTag(tag), singleLine = !multiline,
        minLines = if (multiline) 3 else 1, keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        isError = invalid, supportingText = if (invalid) ({ Text(stringResource(error)) }) else null)
}

/** Confirmation for the local application flow with an explicit return to the home page. */
@Composable
fun TrainerApplicationPendingScreen(onHome: () -> Unit) {
    ScreenFrame(stringResource(R.string.trainer_application_title)) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)) {
            Image(painterResource(R.drawable.ic_trainer_brand),
                stringResource(R.string.brand_logo_description), Modifier.size(120.dp))
            Text(stringResource(R.string.trainer_application_pending), style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.testTag("applicationPending"))
            Text(stringResource(R.string.trainer_application_pending_message), style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onHome, modifier = Modifier.fillMaxWidth().testTag("applicationHome")) {
                Text(stringResource(R.string.trainer_application_home))
            }
        }
    }
}
