package com.bandchu.api.domain.album.model

import java.net.URI

data class Track(
    val id: Long,
    val name: String,
    val url: URI,
    val albumId: Long
)
