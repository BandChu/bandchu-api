package com.bandchu.api.domain.concert.dto

import java.net.URI

data class SubscribedArtistDto(
    val artistId: Long,
    val name: String,
    val description: String?,
    val genre: List<String>,
    val profileImageUrl: URI?,
    val subscribedAt: String,
    val concerts: List<SubscribedConcertDto>
)
