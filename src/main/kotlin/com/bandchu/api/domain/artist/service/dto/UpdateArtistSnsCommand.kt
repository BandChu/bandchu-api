package com.bandchu.api.domain.artist.service.dto

data class UpdateArtistSnsCommand(
    val platform: String,
    val url: String
)