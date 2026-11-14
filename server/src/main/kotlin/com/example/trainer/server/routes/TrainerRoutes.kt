package com.example.trainer.server.routes

import com.example.trainer.server.services.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/** Public discovery and reviews; no client-side catalog substitutes server data. */
fun Route.trainerRoutes(trainers: TrainerService, reviews: ReviewService, bookings: BookingService) {
    get("/trainers") {
        call.respond(trainers.list(
            call.request.queryParameters["specialization"],
            call.numericFilter("maxPrice"), call.numericFilter("minRating"),
        ))
    }
    get("/trainers/{id}") { call.respond(trainers.get(call.pathId())) }
    get("/trainers/{id}/reviews") {
        val id = call.pathId()
        trainers.get(id)
        call.respond(reviews.list(id))
    }
    get("/trainers/{id}/slots") {
        call.respond(bookings.slots(call.pathId(),
            call.request.queryParameters["date"] ?: badRequest("Укажите дату")))
    }
}

