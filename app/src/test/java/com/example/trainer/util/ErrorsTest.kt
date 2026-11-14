package com.example.trainer.util

import java.net.ConnectException
import java.net.SocketTimeoutException
import org.junit.Assert.assertEquals
import org.junit.Test

/** Backend downtime must not be reported as missing Internet access. */
class ErrorsTest {
    @Test fun refusedConnectionIdentifiesBackend() {
        assertEquals("Сервер приложения недоступен. Запустите сервер и повторите вход.",
            ConnectException("Connection refused").userMessage())
    }

    @Test fun slowBackendHasRetryMessage() {
        assertEquals("Сервер не ответил вовремя. Повторите запрос.",
            SocketTimeoutException().userMessage())
    }
}
