package com.bandchu.api.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    @field:NotBlank(message = "리프레시 토큰은 필수입니다.")
    val refreshToken: String
)

