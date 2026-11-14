package com.example.trainer.data

import com.example.trainer.data.auth.FirebaseAuthDataSource
import com.example.trainer.data.remote.interceptor.AuthInterceptor
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/** Ensures Firebase credentials follow session changes and stay on the API origin. */
class AuthInterceptorTest {
    @Test fun credentialsFollowCurrentFirebaseSession() = runTest {
        val firebase = mockk<FirebaseAuthDataSource>()
        coEvery { firebase.getToken(false) } returnsMany listOf("first-user", "second-user")
        val headers = mutableListOf<String?>()
        val engine = MockEngine { request ->
            headers += request.headers[HttpHeaders.Authorization]
            respond("ok")
        }
        HttpClient(engine) { AuthInterceptor(firebase).install(this, "https://api.example/") }.use { client ->
            client.get("https://api.example/trainers")
            client.get("https://api.example/bookings/my")
        }
        assertEquals(listOf("Bearer first-user", "Bearer second-user"), headers)
    }
    @Test fun externalOriginNeverReceivesTokenProactively() = runTest {
        val firebase = mockk<FirebaseAuthDataSource>()
        coEvery { firebase.getToken(false) } returns "private-token"
        var header: String? = "unset"
        HttpClient(MockEngine { request ->
            header = request.headers[HttpHeaders.Authorization]
            respond("ok")
        }) { AuthInterceptor(firebase).install(this, "https://api.example/") }.use {
            it.get("https://images.example/photo")
        }
        assertNull(header)
    }
    @Test fun unauthorizedRefreshesFirebaseTokenOnce() = runTest {
        val firebase = mockk<FirebaseAuthDataSource>()
        var currentToken = "old"
        coEvery { firebase.getToken(false) } coAnswers { currentToken }
        coEvery { firebase.getToken(true) } coAnswers { currentToken = "fresh"; currentToken }
        val sent = mutableListOf<String?>()
        HttpClient(MockEngine { request ->
            val header = request.headers[HttpHeaders.Authorization]
            sent += header
            if (header == "Bearer fresh") respond("ok")
            else respond("expired", HttpStatusCode.Unauthorized,
                headersOf(HttpHeaders.WWWAuthenticate, "Bearer realm=\"Trainer\""))
        }) { AuthInterceptor(firebase).install(this, "https://api.example/") }.use {
            assertEquals(HttpStatusCode.OK, it.get("https://api.example/auth/me").status)
        }
        assertEquals(listOf("Bearer old", "Bearer fresh"), sent)
        coVerify(exactly = 1) { firebase.getToken(true) }
    }
}


