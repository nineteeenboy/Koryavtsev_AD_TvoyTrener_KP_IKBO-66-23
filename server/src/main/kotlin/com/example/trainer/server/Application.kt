package com.example.trainer.server

import com.example.trainer.server.auth.*
import com.example.trainer.server.config.AppConfig
import com.example.trainer.server.db.DatabaseFactory
import com.example.trainer.server.db.seed
import com.example.trainer.server.plugins.*
import com.example.trainer.server.repositories.*
import com.example.trainer.server.services.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import org.koin.dsl.module as koinModule
import org.koin.ktor.plugin.Koin
import org.koin.ktor.ext.get

/** Starts the production server with environment-only credentials. */
fun main() {
    val config = AppConfig.fromEnvironment()
    val database = DatabaseFactory.connect(config)
    database.createTables()
    seed(database)
    embeddedServer(Netty, port = config.port, host = "0.0.0.0") {
        module(config, database)
    }.start(wait = true)
}

/** Installs application dependencies; tests supply isolated DB and verifier. */
fun Application.module(
    config: AppConfig = AppConfig.fromEnvironment(),
    database: DatabaseFactory = DatabaseFactory.connect(config),
    verifier: TokenVerifier = FirebaseAdmin(config.firebaseServiceAccountPath),
) {
    database.createTables()
    // Koin suits a JVM server: explicit constructor wiring without Android annotation processing.
    install(Koin) {
        modules(koinModule {
            single { database }
            single<TokenVerifier> { verifier }
            single { UserRepository(get()) }
            single { TrainerRepository(get()) }
            single { BookingRepository(get()) }
            single { ReviewRepository(get()) }
            single { TrainerWorkspaceRepository(get()) }
            single { TrainerWorkspaceService(get()) }
            single { AuthService(get()) }
            single { TrainerService(get()) }
            single { BookingService(get(), get()) }
            single { ReviewService(get()) }
        })
    }
    install(CallLogging)
    configureSerialization()
    configureStatusPages()
    configureSecurity(get())
    configureRouting(get(), get(), get(), get(), get())
    monitor.subscribe(ApplicationStopped) {
        database.close()
        (verifier as? FirebaseAdmin)?.close()
    }
}


