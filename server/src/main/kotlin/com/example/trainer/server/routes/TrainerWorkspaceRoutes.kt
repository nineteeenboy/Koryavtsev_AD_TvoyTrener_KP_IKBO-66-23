package com.example.trainer.server.routes

import com.example.trainer.server.dto.*
import com.example.trainer.server.services.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/** Every operation requires a verified Firebase account; role checks are server-side. */
fun Route.trainerWorkspaceRoutes(service: TrainerWorkspaceService, auth: AuthService) {
    route("/trainer-applications") {
        get("/my") { call.respond(service.mine(auth.login(call.firebaseUser()))) }
        post {
            call.respond(HttpStatusCode.Created, service.submit(auth.login(call.firebaseUser()), call.receive()))
        }
    }
    route("/admin/trainer-applications") {
        get { call.respond(service.applications(auth.login(call.firebaseUser()))) }
        post("/{id}/decision") {
            call.respond(service.decide(auth.login(call.firebaseUser()), call.pathId(), call.receive()))
        }
    }
    route("/trainer/bookings") {
        get { call.respond(service.sessions(auth.login(call.firebaseUser()))) }
        put("/{id}/note") {
            call.respond(service.saveNote(auth.login(call.firebaseUser()), call.pathId(), call.receive()))
        }
    }
}
