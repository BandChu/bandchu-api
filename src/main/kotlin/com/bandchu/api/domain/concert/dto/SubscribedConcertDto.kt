package com.bandchu.api.domain.concert.dto

data class SubscribedConcertDto(
    val concertId: Long,
    val title: String,
    val place: String,
    val performingSchedule: List<PerformingScheduleDto>,
    val bookingSchedule: String?
)
