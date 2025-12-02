package com.bandchu.api.domain.album.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class TrackRequest(
    @field:NotBlank(message = "트랙명을 입력하세요.")
    @field:Size(max = 30, message = "트랙명은 최대 30자까지 입력 가능합니다.")
    val name: String,
    val url: String
)