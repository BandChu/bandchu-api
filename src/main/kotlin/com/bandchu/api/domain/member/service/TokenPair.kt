package com.bandchu.api.domain.member.service

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)

