package com.bandchu.api.domain.member.service

data class OAuthVerifyResult(
    val accessToken: String,
    val refreshToken: String,
    val memberId: Long
)

