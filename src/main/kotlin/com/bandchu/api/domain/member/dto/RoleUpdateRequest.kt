package com.bandchu.api.domain.member.dto

import com.bandchu.api.domain.member.model.Role
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

data class RoleUpdateRequest(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    @field:NotNull(message = "역할은 필수입니다.")
    val role: Role
)

