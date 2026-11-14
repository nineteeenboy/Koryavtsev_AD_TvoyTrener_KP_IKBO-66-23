package com.example.trainer.server.routes

import com.example.trainer.server.dto.RegisterRequest
import com.example.trainer.server.services.AuthService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/** Firebase already performs registration/login; these endpoints synchronize the SQL profile. */
fun Route.authRoutes(service: AuthService) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            call.respond(service.register(call.firebaseUser(), request.name))
        }
        post("/login") { call.respond(service.login(call.firebaseUser())) }
        get("/me") { call.respond(service.login(call.firebaseUser())) }
    }
}

