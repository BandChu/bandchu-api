package com.bandchu.api.domain.concert.service.dto

import java.time.OffsetDateTime

data class ConcertScheduleCommand(
    val date: OffsetDateTime
)
