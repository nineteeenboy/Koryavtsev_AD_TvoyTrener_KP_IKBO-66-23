package com.example.trainer.domain.usecase

internal fun validateCredentials(email: String, password: String) {
    require(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim())) { "Введите корректный email" }
    require(password.length >= 6) { "Пароль должен содержать минимум 6 символов" }
}

