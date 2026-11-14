package com.example.trainer.server.plugins

import com.example.trainer.server.routes.*
import com.example.trainer.server.services.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.staticResources

/** Registers the REST routes with explicit public/authenticated boundaries. */
fun Application.configureRouting(
    auth: AuthService, trainers: TrainerService, bookings: BookingService, reviews: ReviewService,
    workspace: TrainerWorkspaceService? = null,
) {
    routing {
        staticResources("/media", "media")
        trainerRoutes(trainers, reviews, bookings)
        authenticate("firebase") {
            authRoutes(auth)
            bookingRoutes(bookings, auth)
            reviewRoutes(reviews, auth)
            workspace?.let { trainerWorkspaceRoutes(it, auth) }
        }
    }
}

