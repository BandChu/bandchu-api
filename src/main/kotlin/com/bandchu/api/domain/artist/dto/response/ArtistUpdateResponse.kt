package com.bandchu.api.domain.artist.dto.response

import com.bandchu.api.domain.artist.dto.ArtistSnsDto

data class ArtistUpdateResponse(
    val artistId: Long,
    val name: String,
    val profileImageUrl: String?,
    val description: String?,
    val genre: List<String>,
    val sns: List<ArtistSnsDto>
)
