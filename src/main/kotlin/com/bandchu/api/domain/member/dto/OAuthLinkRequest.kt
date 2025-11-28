package com.bandchu.api.domain.member.dto

import jakarta.validation.constraints.NotBlank

data class OAuthLinkRequest(
    @field:NotBlank(message = "프로바이더는 필수입니다.")
    val provider: String,
    
    @field:NotBlank(message = "토큰은 필수입니다.")
    val token: String
)

