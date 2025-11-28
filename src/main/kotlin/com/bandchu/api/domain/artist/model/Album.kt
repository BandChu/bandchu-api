package com.bandchu.api.domain.artist.model

import kotlinx.datetime.LocalDateTime

data class Album (
    val album_id: Int,
    val name: String,
    val release_date: LocalDateTime,
    val cover_url: String,
    val description: String,
    )