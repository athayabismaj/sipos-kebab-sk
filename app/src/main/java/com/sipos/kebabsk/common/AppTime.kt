package com.sipos.kebabsk.common

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object AppTime {
    val jakartaZone: ZoneId = ZoneId.of("Asia/Jakarta")

    fun todayJakarta(): LocalDate = LocalDate.now(jakartaZone)

    // Tanggal bisnis/shift operasional (toleransi 4 jam untuk shift malam)
    fun businessDateJakarta(): LocalDate = LocalDateTime.now(jakartaZone).minusHours(4).toLocalDate()

    fun nowJakartaDateTime(): LocalDateTime = LocalDateTime.now(jakartaZone)

    fun toEpochMillisAtStartOfDay(date: LocalDate): Long {
        return date.atStartOfDay(jakartaZone).toInstant().toEpochMilli()
    }

    fun dateFromEpochMillis(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(jakartaZone).toLocalDate()
    }
}

