package com.bandchu.api.domain.member.dto

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val memberId: Long,
    val nickname: String
)

