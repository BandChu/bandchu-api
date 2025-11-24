package com.bandchu.api.domain.artist.dto

data class ArtistListItemDto(
    val artistId: Long,
    val name: String,
    val profileImageUrl: String?,
    val createdAt: String
)
