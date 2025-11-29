package com.bandchu.api.domain.artist.model

import java.net.URI

data class SnsLink (
    val id: Long,
    val platform: String,
    val url: URI,
)