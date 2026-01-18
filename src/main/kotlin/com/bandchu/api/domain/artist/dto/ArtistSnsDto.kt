package com.bandchu.api.domain.artist.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
@Schema(description = "앨범 상세 정보")
data class ArtistSnsDto(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    @field:NotBlank(message = "SNS 플랫폼의 종류를 입력하세요.")
    val platform: String,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    @field:NotBlank(message = "SNS URL을 입력하세요.")
    val url: String
)
