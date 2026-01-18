package com.bandchu.api.domain.concert.dto.response

import com.bandchu.api.domain.concert.dto.SubscribedArtistDto
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "구독한 아티스트들에 대한 콘서트 리스트들")
data class ConcertSubscribedResponse(
    val artists: List<SubscribedArtistDto>
)
