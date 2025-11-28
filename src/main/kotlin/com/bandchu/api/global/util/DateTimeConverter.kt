package com.bandchu.api.global.util

import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month

/**
 * kotlinx.datetime.LocalDateTime을 java.time.OffsetDateTime으로 변환하는 확장 함수
 */
fun LocalDateTime.toOffsetDateTime(): OffsetDateTime {
    return java.time.LocalDateTime.of(
        year,
        java.time.Month.valueOf(month.name),
        day,
        hour,
        minute,
        second,
        nanosecond
    ).atOffset(ZoneOffset.UTC)
}

/**
 * java.time.OffsetDateTime을 kotlinx.datetime.LocalDateTime으로 변환하는 확장 함수
 */
fun OffsetDateTime.toKotlinLocalDateTime(): LocalDateTime {
    val javaLocalDateTime = this.toLocalDateTime()
    val month = Month.values()[javaLocalDateTime.monthValue - 1]
    val localDate = LocalDate(javaLocalDateTime.year, month, javaLocalDateTime.dayOfMonth)
    val localTime = LocalTime(
        javaLocalDateTime.hour,
        javaLocalDateTime.minute,
        javaLocalDateTime.second,
        javaLocalDateTime.nano
    )
    return LocalDateTime(localDate, localTime)
}

