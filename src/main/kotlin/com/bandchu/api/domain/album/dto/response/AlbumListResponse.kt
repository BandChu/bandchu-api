package com.bandchu.api.domain.album.dto.response

data class AlbumListResponse(
    val albums: List<AlbumSummaryResponse> = emptyList()
)
