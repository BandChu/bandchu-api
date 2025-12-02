package com.bandchu.api.domain.album.service.dto

import java.net.URI
import java.time.OffsetDateTime

data class CreateAlbumCommand(
    val name: String,
    val coverImageUrl: URI?,
    val releaseDate: OffsetDateTime,
    val description: String?,
    val tracks: List<CreateTrackCommand>
)
