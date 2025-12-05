package com.bandchu.api.domain.member.dto

import com.bandchu.api.domain.member.model.Role
import jakarta.validation.constraints.NotNull

data class RoleUpdateRequest(
    @field:NotNull(message = "역할은 필수입니다.")
    val role: Role
)

