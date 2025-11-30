package com.bandchu.api.domain.concert.dto.response

import com.bandchu.api.domain.concert.dto.SubscribedArtistDto

data class ConcertSubscribedResponse(
    val artists: List<SubscribedArtistDto>
)
