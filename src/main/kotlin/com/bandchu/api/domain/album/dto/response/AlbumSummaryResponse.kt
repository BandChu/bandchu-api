package com.bandchu.api.domain.album.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "콘서트 정보 업데이트 요청")
data class AlbumSummaryResponse(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val albumId: Long,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val name: String,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val coverImageUrl: String?,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val releaseDate: String
)
