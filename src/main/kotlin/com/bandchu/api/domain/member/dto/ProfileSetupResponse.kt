package com.bandchu.api.domain.member.dto

data class ProfileSetupResponse(
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String?
)

