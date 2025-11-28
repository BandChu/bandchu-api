package com.bandchu.api.domain.member.model

import kotlinx.datetime.LocalDateTime

data class Member(
    val id: Long? = null,
    val email: String,
    val password: String,
    val nickname: String,
    val role: Role,
    val googleId: String? = null,
    val profileImageUrl: String? = null,
    val createdAt: LocalDateTime? = null
)