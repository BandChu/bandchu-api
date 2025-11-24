package com.bandchu.api.domain.artist.dto

import kotlinx.datetime.LocalDateTime

data class ArtistListItemDto(
    val artistId: Long,
    val name: String,
    val profileImageUrl: String?,
    val createdAt: LocalDateTime
)
