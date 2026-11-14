package com.example.trainer.data

import com.example.trainer.BuildConfig
import com.example.trainer.data.mapper.toDomain
import com.example.trainer.data.remote.dto.TrainerDto
import org.junit.Assert.*
import org.junit.Test
import java.net.URI

/** Server media URLs resolve against the configured API, retaining profile metadata. */
class TrainerMapperTest {
    @Test fun relativePortraitUsesConfiguredServer() {
        val dto = TrainerDto(13, "Инструктор", "Сила", "/media/trainers/trainer_1.jpg",
            "Достижения", 8, 2500.0, 0.0, 0, true)
        val trainer = dto.toDomain()
        assertEquals(URI(BuildConfig.API_BASE_URL).resolve(dto.photoUrl).toString(), trainer.photoUrl)
        assertTrue(trainer.isFeatured)
        assertEquals(dto.description, trainer.description)
    }
    @Test fun absolutePortraitRemainsAbsolute() {
        val dto = TrainerDto(1, "Тренер", "Бокс", "https://example.com/photo.jpg", "", 3, 1000.0, 0.0, 0)
        assertEquals(dto.photoUrl, dto.toDomain().photoUrl)
        assertFalse(dto.toDomain().isFeatured)
    }
}
