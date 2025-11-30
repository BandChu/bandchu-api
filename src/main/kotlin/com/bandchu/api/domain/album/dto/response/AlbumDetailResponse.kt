package com.bandchu.api.domain.album.dto.response

import java.net.URI
import java.time.OffsetDateTime

data class AlbumDetailResponse(
    val albumId: Long,
    val name: String,
    val coverImageUrl: String?,
    val releaseDate: String,
    val description: String?,
    val tracks: List<TrackResponse>
)
