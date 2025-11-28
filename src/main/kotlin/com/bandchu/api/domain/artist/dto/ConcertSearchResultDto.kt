package com.bandchu.api.domain.artist.dto

data class ConcertSearchResultDto(
    val concertId: Long,
    val title: String,
    val place: String,
    val posterImageUrl: String?
)
