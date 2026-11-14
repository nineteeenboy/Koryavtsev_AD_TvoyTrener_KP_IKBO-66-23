package com.example.trainer.server.services

import io.ktor.http.HttpStatusCode

/** Business failure with a stable HTTP status and machine-readable code. */
class ApiException(val status: HttpStatusCode, val code: String, message: String) : RuntimeException(message)
fun badRequest(message: String): Nothing = throw ApiException(HttpStatusCode.BadRequest, "INVALID_REQUEST", message)
fun notFound(message: String): Nothing = throw ApiException(HttpStatusCode.NotFound, "NOT_FOUND", message)
fun conflict(message: String): Nothing = throw ApiException(HttpStatusCode.Conflict, "CONFLICT", message)
fun forbidden(message: String = "Недостаточно прав"): Nothing =
    throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", message)

