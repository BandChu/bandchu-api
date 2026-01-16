package com.bandchu.api.domain.album.dto.response

data class AlbumSummaryResponse(
    val albumId: Long,
    val name: String,
    val coverImageUrl: String?,
    val releaseDate: String
)
