package com.example.trainer.util

import java.time.*
import java.time.format.DateTimeFormatter

/** Booking UI consistently displays the gym's timezone. */
val gymZone: ZoneId = ZoneId.of("Europe/Moscow")
fun displayInstant(value: String): String =
    Instant.parse(value).atZone(gymZone).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + " МСК"
fun displayTime(value: String): String =
    Instant.parse(value).atZone(gymZone).format(DateTimeFormatter.ofPattern("HH:mm"))

