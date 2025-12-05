package com.bandchu.api.domain.member.dto

data class MemberInfo(
    val userId: Long,
    val username: String,
    val profileImageUrl: String? = null
)
