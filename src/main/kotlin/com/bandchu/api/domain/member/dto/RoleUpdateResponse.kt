package com.bandchu.api.domain.member.dto

import com.bandchu.api.domain.member.model.Role

data class RoleUpdateResponse(
    val memberId: Long,
    val role: Role
)

