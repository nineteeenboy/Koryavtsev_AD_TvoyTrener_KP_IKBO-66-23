package com.example.trainer.util

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

/** Converts SDK/network failures into readable user messages. */
fun Throwable.userMessage(): String = when (this) {
    is FirebaseNetworkException -> "Не удалось подключиться к Firebase. Проверьте интернет и повторите."
    is ConnectException -> "Сервер приложения недоступен. Запустите сервер и повторите вход."
    is SocketTimeoutException -> "Сервер не ответил вовремя. Повторите запрос."
    is IOException -> "Не удалось подключиться к серверу приложения. Проверьте подключение и доступность сервера."
    is FirebaseAuthException -> when (errorCode) {
        "ERROR_EMAIL_ALREADY_IN_USE" -> "Этот email уже зарегистрирован"
        "ERROR_INVALID_EMAIL" -> "Введите корректный email"
        "ERROR_WEAK_PASSWORD" -> "Пароль должен содержать минимум 6 символов"
        "ERROR_TOO_MANY_REQUESTS" -> "Слишком много попыток. Попробуйте позже."
        else -> "Не удалось войти. Проверьте email и пароль."
    }
    else -> message ?: "Не удалось выполнить запрос"
}

