package com.bandchu.api.domain.artist.model

import kotlinx.datetime.LocalDateTime

data class Concert (
    val concert_id: Int,
    val title: String,
    val place: String,
    val poster_url: String?,
    val information: String?,
    val booking_date: LocalDateTime?,
    val booking_url: String?,
    val created_at: LocalDateTime,


    )