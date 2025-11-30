package com.bandchu.api.domain.member.service

data class LoginResult(
    val accessToken: String,
    val refreshToken: String,
    val memberId: Long,
    val nickname: String
)

