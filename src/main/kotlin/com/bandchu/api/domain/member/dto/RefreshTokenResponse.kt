package com.bandchu.api.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema

data class RefreshTokenResponse(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val accessToken: String,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val refreshToken: String
)

