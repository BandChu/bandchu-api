package com.bandchu.api.domain.artist.dto

data class ArtistSearchResultDto(
    val artistId: Long,
    val name: String,
    val profileImageUrl: String?
)
