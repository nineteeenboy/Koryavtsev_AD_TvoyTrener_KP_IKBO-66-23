package com.example.trainer.server.routes

import com.example.trainer.server.dto.BookingRequest
import com.example.trainer.server.services.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/** Authenticated booking operations enforce ownership on the server. */
fun Route.bookingRoutes(service: BookingService, auth: AuthService) {
    post("/trainers/{id}/bookings") {
        val request = call.receive<BookingRequest>()
        auth.login(call.firebaseUser())
        call.respond(HttpStatusCode.Created, service.book(call.firebaseUser().uid, call.pathId(), request))
    }
    get("/bookings/my") { call.respond(service.mine(call.firebaseUser().uid)) }
    delete("/bookings/{id}") {
        service.cancel(call.firebaseUser().uid, call.pathId())
        call.respond(HttpStatusCode.NoContent)
    }
}

