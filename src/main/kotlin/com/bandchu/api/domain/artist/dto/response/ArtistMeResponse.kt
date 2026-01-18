package com.bandchu.api.domain.artist.dto.response

import com.bandchu.api.domain.album.dto.response.AlbumSummaryResponse
import com.bandchu.api.domain.concert.dto.response.ConcertDetailResponse
import io.swagger.v3.oas.annotations.media.Schema
@Schema(description = "아티스트 본인에 대한 정보")
data class ArtistMeResponse(
    @get:Schema(description = "존재여부", example = "아티스트 본인에 대한 정보가 있는지")
    val isExists: Boolean,

    @get:Schema(description = "아티스트 정보", example = "데이먼스이어 단독콘서트")
    val artist: ArtistDetailResponse?,

    @get:Schema(description = "앨범 ", example = "데이먼스이어 단독콘서트")
    val albums: List<AlbumSummaryResponse> = emptyList(),

    @get:Schema(description = "공연 디테일 정보", example = "펜타포트, 단독콘서트, 락페")
    val concerts: List<ConcertDetailResponse> = emptyList()
)
