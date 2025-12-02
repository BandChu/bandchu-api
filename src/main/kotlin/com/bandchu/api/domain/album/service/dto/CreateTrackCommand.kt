package com.bandchu.api.domain.album.service.dto

import java.net.URI

data class CreateTrackCommand(
    val name: String,
    val url: URI
)