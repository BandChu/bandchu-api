package com.bandchu.api.domain.album.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "앨범 리스트 응답")
data class AlbumListResponse(
    val albums: List<AlbumSummaryResponse> = emptyList()
)
