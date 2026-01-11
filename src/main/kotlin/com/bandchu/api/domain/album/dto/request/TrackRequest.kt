package com.bandchu.api.domain.album.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
@Schema(description = "콘서트 정보 업데이트 요청")
data class TrackRequest(
    @get:Schema(description = "앨범 내 노래 이름", example = "너의 기사 (데이먼스이어 headache 앨범 내 수록곡)")
    @field:NotBlank(message = "트랙명을 입력하세요.")
    @field:Size(max = 30, message = "트랙명은 최대 30자까지 입력 가능합니다.")
    val name: String,

    @get:Schema(description = "곡 url", example = "곡 url")
    val url: String
)