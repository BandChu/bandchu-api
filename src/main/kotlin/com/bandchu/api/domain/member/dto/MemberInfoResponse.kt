package com.bandchu.api.domain.member.dto

import com.bandchu.api.domain.member.model.Role

data class MemberInfoResponse(
    val memberId: Long,
    val email: String,
    val nickname: String,
    val role: Role,
    val profileImageUrl: String?
)

