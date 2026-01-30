package com.bandchu.api.domain.artist.dto.response

import com.bandchu.api.domain.artist.dto.ArtistListItemDto
import io.swagger.v3.oas.annotations.media.Schema
@Schema(description = "앨범 상세 정보")
data class ArtistListResponse(
    @get:Schema(description = "아티스트 리스트 아이템 디티오", example = "아티스트 리스트 아이템 즉 아티스트에 대한 정보에 대한 리스트를 객체로 가짐")
    val artists: List<ArtistListItemDto>
)
