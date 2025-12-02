package com.bandchu.api.domain.member.dto

data class GoogleOAuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNewMember: Boolean,
    val memberId: Long,
    val nickname: String
)

