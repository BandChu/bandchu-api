package com.bandchu.api.domain.concert.dto.response

import com.bandchu.api.domain.concert.PerformingScheduleDto
import java.time.OffsetDateTime

data class ConcertUpdateResponse(
    val concertId: Long,
    val title: String,
    val place: String,
    val posterImageUrl: String?,
    val information: String?,
    val bookingSchedule: String?,
    val bookingUrl: String?,
    val performingSchedule: List<PerformingScheduleDto> = emptyList(),
    val createdAt: OffsetDateTime
)
