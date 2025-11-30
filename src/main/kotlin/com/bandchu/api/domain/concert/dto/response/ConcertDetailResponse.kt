package com.bandchu.api.domain.concert.dto.response

data class ConcertDetailResponse(
    val concertId: Long,
    val title: String,
    val place: String,
    val posterImageUrl: String?,
    val information: String?,
    val bookingSchedule: String?,
    val bookingUrl: String?,
    val performingSchedule: List<ConcertScheduleResponse> = emptyList(),
    val createdAt: String
)
