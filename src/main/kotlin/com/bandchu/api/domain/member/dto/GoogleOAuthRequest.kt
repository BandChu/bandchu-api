package com.bandchu.api.domain.member.dto

import jakarta.validation.constraints.NotBlank

data class GoogleOAuthRequest(
    @field:NotBlank(message = "구글 ID 토큰은 필수입니다.")
    val idToken: String
)

