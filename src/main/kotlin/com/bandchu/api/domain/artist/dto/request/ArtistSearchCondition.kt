package com.bandchu.api.domain.artist.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
@Schema(description = "앨범 상세 정보")
data class ArtistSearchCondition(
    @get:Schema(description = "아티스트 검색 조건", example = "데이먼스이어")
    @field:NotBlank("검색어를 입력하세요.")
    val keyword: String,
)
