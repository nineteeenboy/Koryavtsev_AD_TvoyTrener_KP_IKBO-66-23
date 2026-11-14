package com.example.trainer.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.example.trainer.R
import com.example.trainer.domain.model.User
import com.example.trainer.presentation.components.*
import com.example.trainer.util.UiState

/** Email/password login form; callbacks keep UI independently testable. */
@Composable
fun LoginScreen(state: UiState<User>, onLogin: (String, String) -> Unit, onRegister: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    // Password intentionally stays only in memory.
    var password by remember { mutableStateOf("") }
    val busy = state == UiState.Loading
    ScreenFrame("Твой тренер") { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
            .imePadding().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Тренируйтесь с тем, кто понимает ваши цели", style = MaterialTheme.typography.headlineMedium)
            Text("Войдите, чтобы выбрать тренера и записаться на занятие")
            OutlinedTextField(email, { email = it }, label = { Text(stringResource(R.string.email)) },
                singleLine = true, enabled = !busy, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().testTag("email"))
            OutlinedTextField(password, { password = it }, label = { Text(stringResource(R.string.password)) },
                singleLine = true, enabled = !busy, visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().testTag("password"))
            ActionStatus(state)
            Button(onClick = { onLogin(email, password) }, enabled = !busy && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth().testTag("login")) { Text(stringResource(R.string.login)) }
            TextButton(onClick = onRegister, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Text("Создать аккаунт")
            }
        }
    }
}

