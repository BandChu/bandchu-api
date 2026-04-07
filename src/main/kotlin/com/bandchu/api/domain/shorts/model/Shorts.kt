package com.bandchu.api.domain.shorts.model

import java.net.URI
import java.time.OffsetDateTime

data class Shorts(
    val id: Long,
    val artistProfileId: Long,
    val title: String,
    val description: String?,
    val videoUrl: URI,
    val thumbnailUrl: URI?,
    val viewCount: Long,
    val shareCount: Long,
    val createdAt: OffsetDateTime
)