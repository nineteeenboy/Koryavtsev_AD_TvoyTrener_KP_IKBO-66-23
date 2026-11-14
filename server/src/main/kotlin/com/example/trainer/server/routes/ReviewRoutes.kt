package com.example.trainer.server.routes

import com.example.trainer.server.dto.ReviewRequest
import com.example.trainer.server.services.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/** Authenticated review publication and personal review history. */
fun Route.reviewRoutes(service: ReviewService, auth: AuthService) {
    post("/trainers/{id}/reviews") {
        val request = call.receive<ReviewRequest>()
        auth.login(call.firebaseUser())
        call.respond(HttpStatusCode.Created, service.create(call.firebaseUser().uid, call.pathId(), request))
    }
    get("/reviews/my") { call.respond(service.mine(call.firebaseUser().uid)) }
}

