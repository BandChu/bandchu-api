package com.bandchu.api.domain.artist.model

import kotlinx.datetime.LocalDateTime

data class ArtiProfile (
    val id: Long,
    val artistName: String,
    val genre: List<String>,
    val description: String,
    val profileImageUrl: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val memberId: Long,
)