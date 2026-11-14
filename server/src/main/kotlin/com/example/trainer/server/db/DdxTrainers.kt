package com.example.trainer.server.db

import com.example.trainer.server.db.tables.Trainers
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/** Curated public profile snapshot with catalog pricing. */
@Serializable
private data class DdxProfile(
    val sourceId: Int, val name: String, val specialty: String, val experienceYears: Int,
    val summary: String, val sourceUrl: String, val portraitSourceUrl: String,
    val price: Int,
)

private val legacyNames = listOf(
    "Алексей Орлов", "Мария Соколова", "Дмитрий Волков", "Анна Кузнецова",
    "Илья Морозов", "Елена Смирнова", "Сергей Попов", "Ольга Лебедева",
    "Максим Новиков", "Дарья Павлова", "Андрей Фёдоров", "Виктория Иванова",
)

private val ddxProfiles by lazy {
    val content = checkNotNull(DdxProfile::class.java.getResourceAsStream("/catalog/ddx-trainers.json"))
        .bufferedReader(Charsets.UTF_8).use { it.readText() }
    Json.decodeFromString<List<DdxProfile>>(content)
}

/** Replaces only known legacy random-avatar rows and refreshes official profiles in place. */
fun seedDdxTrainers(database: DatabaseFactory) = transaction(database.database) {
    val rows = Trainers.selectAll().toList()
    ddxProfiles.forEachIndexed { index, profile ->
        val photo = "/media/ddx/${profile.sourceId}.jpg"
        val existing = rows.firstOrNull { it[Trainers.photoUrl] == photo }
            ?: rows.firstOrNull {
                it[Trainers.name] == legacyNames[index] &&
                    it[Trainers.photoUrl].startsWith("https://i.pravatar.cc/")
            }
        val text = "О тренере\n${profile.summary}\n\n" +
            "Источник\nDDX Fitness: ${profile.sourceUrl}"
        if (existing == null) {
            Trainers.insert {
                it[name] = profile.name
                it[specialization] = profile.specialty
                it[photoUrl] = photo
                it[description] = text
                it[experienceYears] = profile.experienceYears
                it[price] = profile.price.toBigDecimal()
            }
        } else {
            Trainers.update({ Trainers.id eq existing[Trainers.id] }) {
                it[name] = profile.name
                it[specialization] = profile.specialty
                it[photoUrl] = photo
                it[description] = text
                it[experienceYears] = profile.experienceYears
            }
        }
    }
}

