package com.bandchu.api.domain.concert.service.dto

import java.net.URI
import java.time.OffsetDateTime

data class CreateConcertCommand(
    val title: String,
    val place: String,
    val posterImageUrl: URI?,
    val information: String?,
    val bookingSchedule: OffsetDateTime?,
    val bookingUrl: URI?,
    val performingSchedule: List<ConcertScheduleCommand> = emptyList()
)