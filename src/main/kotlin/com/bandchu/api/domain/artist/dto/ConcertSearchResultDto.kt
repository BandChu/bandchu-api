package com.bandchu.api.domain.artist.dto

import io.swagger.v3.oas.annotations.media.Schema
@Schema(description = "앨범 상세 정보")
data class ConcertSearchResultDto(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val concertId: Long,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val title: String,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val place: String,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val posterImageUrl: String?
)
