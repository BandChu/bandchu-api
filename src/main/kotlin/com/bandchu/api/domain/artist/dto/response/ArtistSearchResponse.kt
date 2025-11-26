package com.bandchu.api.domain.artist.dto.response

import com.bandchu.api.domain.artist.dto.ArtistSearchResultDto
import com.bandchu.api.domain.artist.dto.ConcertSearchResultDto

data class ArtistSearchResponse(
    val artists: List<ArtistSearchResultDto>,
    val concerts: List<ConcertSearchResultDto>
)
