package com.bandchu.api.domain.concert.model

import java.time.OffsetDateTime

data class ConcertSchedule(
    val id: Long,
    val date: OffsetDateTime,
    val concertId: Long
)
