package com.bandchu.api.domain.artist.dto.response

import com.bandchu.api.domain.artist.dto.ArtistListItemDto

data class ArtistListResponse(
    val artists: List<ArtistListItemDto>
)
