package com.example.trainer.data.remote.interceptor

import com.example.trainer.data.auth.FirebaseAuthDataSource
import io.ktor.client.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import java.net.URI
import javax.inject.Inject

/** Supplies current Firebase ID tokens and refreshes once after HTTP 401. */
class AuthInterceptor @Inject constructor(private val firebase: FirebaseAuthDataSource) {
    fun install(config: HttpClientConfig<*>, baseUrl: String) {
        val origin = URI(baseUrl)
        val originPort = if (origin.port != -1) origin.port else if (origin.scheme == "https") 443 else 80
        config.install(Auth) {
            bearer {
                loadTokens { firebase.getToken()?.let { BearerTokens(it, "") } }
                refreshTokens { firebase.getToken(true)?.let { BearerTokens(it, "") } }
                cacheTokens = false
                sendWithoutRequest { request ->
                    // URLBuilder.port is unspecified for default ports; Url.port resolves it.
                    val url = request.url.build()
                    url.host == origin.host && url.port == originPort && url.protocol.name == origin.scheme
                }
            }
        }
    }
}

