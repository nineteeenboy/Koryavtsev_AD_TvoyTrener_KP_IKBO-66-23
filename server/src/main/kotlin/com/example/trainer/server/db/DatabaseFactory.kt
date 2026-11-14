package com.example.trainer.server.db

import com.example.trainer.server.config.AppConfig
import com.example.trainer.server.db.tables.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

/** Owns the connection pool and executes database work on the IO dispatcher. */
class DatabaseFactory(val database: Database, private val pool: HikariDataSource? = null) : AutoCloseable {
    suspend fun <T> query(block: Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }

    fun createTables() = transaction(database) {
        SchemaUtils.create(Users, Trainers, Bookings, Reviews)
        SchemaUtils.create(TrainerApplications, TrainerAccounts, TrainingNotes)
    }
    override fun close() { pool?.close() }

    companion object {
        fun connect(config: AppConfig): DatabaseFactory {
            val url = config.databaseUrl
            val jdbcUrl = when {
                url.startsWith("jdbc:postgresql://") -> url
                url.startsWith("postgresql://") -> "jdbc:$url"
                url.startsWith("postgres://") -> "jdbc:postgresql://" + url.removePrefix("postgres://")
                else -> error("DATABASE_URL must be a PostgreSQL URL")
            }
            require(!java.net.URI(jdbcUrl.removePrefix("jdbc:")).rawAuthority.contains("@")) {
                "Supply credentials via DATABASE_USER and DATABASE_PASSWORD"
            }
            val securedUrl = if (jdbcUrl.contains("sslmode=")) jdbcUrl
                else jdbcUrl + (if ('?' in jdbcUrl) "&" else "?") + "sslmode=require"
            val pool = HikariDataSource(HikariConfig().apply {
                this.jdbcUrl = securedUrl
                username = config.databaseUser
                password = config.databasePassword
                driverClassName = "org.postgresql.Driver"
                maximumPoolSize = 5
                minimumIdle = 0
                connectionTimeout = 30_000
                validationTimeout = 5_000
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_READ_COMMITTED"
            })
            return DatabaseFactory(Database.connect(pool), pool)
        }
    }
}

