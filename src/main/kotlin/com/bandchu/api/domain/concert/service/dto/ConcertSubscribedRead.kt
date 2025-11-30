package com.bandchu.api.domain.concert.service.dto

import com.bandchu.api.domain.artist.model.ArtiProfile
import com.bandchu.api.domain.concert.model.Concert
import java.time.OffsetDateTime

data class ConcertSubscribedRead(
    val artists: ArtiProfile,
    val subscribedAt: OffsetDateTime,
    val concerts: List<Concert>
)
