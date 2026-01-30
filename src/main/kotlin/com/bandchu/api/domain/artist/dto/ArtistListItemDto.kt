package com.bandchu.api.domain.artist.dto

import io.swagger.v3.oas.annotations.media.Schema
@Schema(description = "앨범 상세 정보")
data class ArtistListItemDto(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val artistId: Long,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val name: String,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val profileImageUrl: String?,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val createdAt: String
)
