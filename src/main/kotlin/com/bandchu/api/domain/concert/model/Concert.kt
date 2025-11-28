package com.bandchu.api.domain.concert.model

import java.net.URI
import java.time.OffsetDateTime

data class Concert(
    val id: Long,
    val title: String,
    val place: String,
    val posterImageUrl: URI?,
    val information: String?,
    val bookingUrl: URI?,
    val bookingSchedule: OffsetDateTime?,
    val createdAt: OffsetDateTime,
    val artiProfileId: Long
)
