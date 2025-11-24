package com.bandchu.api.domain.artist.model

import kotlinx.datetime.LocalDateTime

data class ArtiProfile (
    val artiprofile_id: Int,
    val artist_name: String,
    val genre: List<Int>,
    val description: String,
    val profile_image_url: String,
    val created_at: LocalDateTime,
    val updated_at: LocalDateTime,
)