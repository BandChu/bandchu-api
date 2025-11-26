package com.bandchu.api.domain.artist.service.dto

import com.bandchu.api.domain.artist.model.Genre

data class UpdateArtistDetailCommand(
    val artistId: Long,
    val name: String,
    val profileImageUrl: String?,
    val description: String?,
    val genre: List<Genre>,
    val sns: List<UpdateArtistSnsCommand>
)
