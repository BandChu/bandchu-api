package com.bandchu.api.domain.member.model

import kotlinx.datetime.LocalDateTime

data class Member (
    val member_id: Int? = null,
    val email : String,
    val password : String,
    val created_at: LocalDateTime? = null,
    val role: Role? = null,
)