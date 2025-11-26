package com.bandchu.api.domain.artist.model

import java.net.URI
import java.time.OffsetDateTime

data class ArtiProfile (
    val id: Long,
    val artistName: String,
    val genre: List<String>,
    val description: String?,
    val profileImageUrl: URI?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val memberId: Long,
)