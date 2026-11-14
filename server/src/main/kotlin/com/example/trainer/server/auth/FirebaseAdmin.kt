package com.example.trainer.server.auth

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import io.ktor.server.auth.Principal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream

/** Claims accepted only after Firebase Admin verifies signature, audience, expiry and revocation. */
data class FirebasePrincipal(val uid: String, val email: String, val name: String?) : Principal
/** Authentication boundary allows route tests to replace Firebase verification. */
fun interface TokenVerifier {
    suspend fun verifyToken(token: String): FirebasePrincipal?
}
/** Production Firebase Admin token verifier. */
class FirebaseAdmin(serviceAccountPath: String) : TokenVerifier {
    private val app = FileInputStream(serviceAccountPath).use {
        FirebaseApp.initializeApp(
            FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(it)).build(),
            "trainer-" + java.util.UUID.randomUUID(),
        )
    }
    private val auth = FirebaseAuth.getInstance(app)
    override suspend fun verifyToken(token: String): FirebasePrincipal? = withContext(Dispatchers.IO) {
        try {
            val decoded = auth.verifyIdToken(token, true)
            val email = decoded.email ?: return@withContext null
            FirebasePrincipal(decoded.uid, email, decoded.name)
        } catch (_: FirebaseAuthException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }
    fun close() = app.delete()
}

