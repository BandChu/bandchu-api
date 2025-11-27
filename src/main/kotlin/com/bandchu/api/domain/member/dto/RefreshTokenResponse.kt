package com.bandchu.api.domain.member.dto

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)

