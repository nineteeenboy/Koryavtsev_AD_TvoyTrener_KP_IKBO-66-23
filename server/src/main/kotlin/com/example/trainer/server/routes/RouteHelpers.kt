package com.example.trainer.server.routes

import com.example.trainer.server.auth.FirebasePrincipal
import com.example.trainer.server.services.badRequest
import io.ktor.server.application.*
import io.ktor.server.auth.*

/** Reads identifiers without silently accepting malformed values. */
fun ApplicationCall.pathId(name: String = "id"): Int =
    parameters[name]?.toIntOrNull()?.takeIf { it > 0 } ?: badRequest("Некорректный идентификатор")
fun ApplicationCall.firebaseUser(): FirebasePrincipal =
    principal<FirebasePrincipal>() ?: error("Protected route without principal")
fun ApplicationCall.numericFilter(name: String): Double? =
    request.queryParameters[name]?.let { it.toDoubleOrNull() ?: badRequest("Некорректный фильтр $name") }

