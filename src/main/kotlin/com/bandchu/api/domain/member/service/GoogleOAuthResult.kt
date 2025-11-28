package com.bandchu.api.domain.member.service

data class GoogleOAuthResult(
    val accessToken: String,
    val refreshToken: String,
    val isNewMember: Boolean,
    val memberId: Long,
    val nickname: String
)

