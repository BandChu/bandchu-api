package com.bandchu.api.domain.artist.service.dto

import com.bandchu.api.domain.artist.model.Genre
import java.net.URI

data class CreateArtistDetailCommand(
    val name: String,
    val profileImageUrl: URI?,
    val description: String?,
    val genre: List<Genre>,
    val sns: List<ArtistSnsCommand>
)
