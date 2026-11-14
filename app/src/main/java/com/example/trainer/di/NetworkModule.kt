package com.example.trainer.di

import com.example.trainer.BuildConfig
import com.example.trainer.data.remote.dto.ErrorDto
import com.example.trainer.data.remote.interceptor.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CancellationException
import javax.inject.Singleton

/** Configures one authenticated Ktor client; HTTP logging never includes tokens. */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun client(auth: AuthInterceptor): HttpClient = HttpClient(OkHttp) {
        expectSuccess = true
        followRedirects = false
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
        install(HttpTimeout) { requestTimeoutMillis = 30_000; connectTimeoutMillis = 15_000 }
        defaultRequest {
            url(BuildConfig.API_BASE_URL.trimEnd('/') + "/")
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
        auth.install(this, BuildConfig.API_BASE_URL)
        HttpResponseValidator {
            handleResponseExceptionWithRequest { cause, _ ->
                if (cause is ResponseException) {
                    val error = try { cause.response.body<ErrorDto>() } catch (e: CancellationException) { throw e } catch (_: Exception) { null }
                    throw IllegalStateException(error?.message ?: "Ошибка сервера (${cause.response.status.value})", cause)
                }
            }
        }
    }
}


