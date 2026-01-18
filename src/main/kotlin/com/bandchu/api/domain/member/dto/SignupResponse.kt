package com.bandchu.api.domain.member.dto

import com.bandchu.api.domain.member.model.Role
import java.time.OffsetDateTime

data class SignupResponse(
    val memberId: Long,
    val email: String,
    val nickname: String,
    val role: Role,
    val accessToken: String,
    val refreshToken: String,
    val createdAt: OffsetDateTime
)

