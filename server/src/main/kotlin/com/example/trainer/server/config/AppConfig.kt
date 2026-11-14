package com.example.trainer.server.config

/** Runtime configuration; credentials are supplied exclusively by environment variables. */
data class AppConfig(
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val firebaseServiceAccountPath: String,
    val port: Int = 8080,
) {
    companion object {
        fun fromEnvironment(): AppConfig {
            fun required(name: String) = System.getenv(name)?.takeIf { it.isNotBlank() }
                ?: error("Environment variable $name is required")
            return AppConfig(
                required("DATABASE_URL"), required("DATABASE_USER"),
                required("DATABASE_PASSWORD"), required("FIREBASE_SERVICE_ACCOUNT_PATH"),
                System.getenv("PORT")?.toIntOrNull() ?: 8080,
            )
        }
    }
}

