package com.bandchu.api.domain.member.model

import kotlinx.datetime.LocalDateTime

data class Member(
    val id: Long? = null,
    val email: String,
    val password: String,
    val nickname: String,
    val role: Role,
    val createdAt: LocalDateTime? = null
)