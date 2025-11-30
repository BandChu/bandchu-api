package com.bandchu.api.domain.album.dto.response

import java.net.URI

data class TrackResponse(
    val trackId: Long,
    val name: String,
    val url: String
)
