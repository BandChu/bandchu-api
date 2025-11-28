package com.bandchu.api.domain.member.dto

data class OAuthVerifyResponse(
    val accessToken: String,
    val refreshToken: String,
    val memberId: Long
)

