package com.example.trainer.server.db

import com.example.trainer.server.config.AppConfig

/** Refreshes both bundled catalogs while preserving IDs and existing user records. */
fun seed(database: DatabaseFactory) {
    seedDdxTrainers(database)
    seedFeaturedTrainers(database)
}

/** Runs the same idempotent catalog refresh as the production server. */
fun main() {
    DatabaseFactory.connect(AppConfig.fromEnvironment()).use {
        it.createTables()
        seed(it)
    }
}
