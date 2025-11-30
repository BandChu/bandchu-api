package com.bandchu.api.domain.album.model

import java.net.URI
import java.time.OffsetDateTime

data class Album(
    val id: Long,
    val name: String,
    val releaseDate: OffsetDateTime,
    val coverImageUrl: URI?,
    val description: String?,
    val artiProfileId: Long,
    val tracks: List<Track> = emptyList()
)
