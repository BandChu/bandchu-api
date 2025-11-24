package com.bandchu.api.domain.artist.dto

import com.bandchu.api.domain.artist.dto.response.ArtistListResponse
import com.bandchu.api.domain.artist.model.ArtiProfile

fun ArtiProfile.toArtistListItemDto(): ArtistListItemDto =
    ArtistListItemDto(
        artistId = id,
        name = artistName,
        profileImageUrl = profileImageUrl,
        createdAt = createdAt.toString(),
    )

fun List<ArtiProfile>.toArtistListResponse(): ArtistListResponse =
    ArtistListResponse(
        artists = this.map { it.toArtistListItemDto() },
    )