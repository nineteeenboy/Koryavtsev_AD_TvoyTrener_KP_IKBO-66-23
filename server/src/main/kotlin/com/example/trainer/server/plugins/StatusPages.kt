package com.example.trainer.server.plugins

import com.example.trainer.server.dto.ErrorDto
import com.example.trainer.server.services.ApiException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

/** Maps failures to safe JSON responses without disclosing SQL or credentials. */
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(cause.status, ErrorDto(cause.code, cause.message ?: "Ошибка"))
        }
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorDto("INVALID_REQUEST", "Некорректный запрос"))
        }
        exception<SerializationException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorDto("INVALID_JSON", "Некорректный JSON"))
        }
        exception<Throwable> { call, cause ->
            if (cause is CancellationException) throw cause
            call.application.log.error("Request failed: ${cause.javaClass.simpleName}")
            call.respond(HttpStatusCode.InternalServerError, ErrorDto("INTERNAL_ERROR", "Ошибка сервера. Повторите позже"))
        }
        status(HttpStatusCode.Unauthorized) { call, status ->
            call.respond(status, ErrorDto("UNAUTHORIZED", "Войдите в аккаунт"))
        }
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(status, ErrorDto("NOT_FOUND", "Ресурс не найден"))
        }
    }
}

