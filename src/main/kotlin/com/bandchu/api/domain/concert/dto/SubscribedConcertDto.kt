package com.bandchu.api.domain.concert.dto

import com.bandchu.api.domain.concert.dto.response.ConcertScheduleResponse

data class SubscribedConcertDto(
    val concertId: Long,
    val title: String,
    val place: String,
    val bookingUrl: String?,
    val performingSchedule: List<ConcertScheduleResponse>,
    val bookingSchedule: String?
)
