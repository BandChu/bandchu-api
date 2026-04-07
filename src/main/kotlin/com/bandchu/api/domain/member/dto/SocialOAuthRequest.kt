package com.bandchu.api.domain.member.dto

import jakarta.validation.constraints.NotBlank

data class SocialOAuthRequest(
    @field:NotBlank(message = "인증 토큰은 필수입니다.")
    val token: String // 구글의 idToken, 카카오/네이버의 accessToken을 모두 이 필드로 받음
)