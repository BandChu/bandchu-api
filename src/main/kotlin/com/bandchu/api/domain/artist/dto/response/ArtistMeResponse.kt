package com.bandchu.api.domain.artist.dto.response

import com.bandchu.api.domain.album.dto.response.AlbumSummaryResponse
import com.bandchu.api.domain.concert.dto.response.ConcertDetailResponse

data class ArtistMeResponse(
    val isExists: Boolean,
    val artist: ArtistDetailResponse?,
    val albums: List<AlbumSummaryResponse> = emptyList(),
    val concerts: List<ConcertDetailResponse> = emptyList()
)
