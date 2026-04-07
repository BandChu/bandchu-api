package com.bandchu.api.domain.concert.service.dto

import java.math.BigDecimal
import java.net.URI
import java.time.OffsetDateTime

data class CreateConcertCommand(
    val title: String,
    val place: String,
    val posterImageUrl: URI?,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    val information: String?,
    val bookingSchedule: OffsetDateTime?,
    val bookingUrl: URI?,
    val performingSchedule: List<ConcertScheduleCommand> = emptyList()
)