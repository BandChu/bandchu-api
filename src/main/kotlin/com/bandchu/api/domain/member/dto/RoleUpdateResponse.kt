package com.bandchu.api.domain.member.dto

import com.bandchu.api.domain.member.model.Role
import io.swagger.v3.oas.annotations.media.Schema

data class RoleUpdateResponse(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val memberId: Long,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val role: Role
)

