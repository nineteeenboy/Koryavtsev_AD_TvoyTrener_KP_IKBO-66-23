package com.example.trainer.server.plugins

import com.example.trainer.server.auth.TokenVerifier
import io.ktor.server.application.*
import io.ktor.server.auth.*

/** Firebase Admin validates JWT signatures and claims using Google's rotating keys. */
fun Application.configureSecurity(verifier: TokenVerifier) {
    install(Authentication) {
        bearer("firebase") {
            realm = "Trainer"
            authenticate { credential -> verifier.verifyToken(credential.token) }
        }
    }
}

