package com.bandchu.api.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class ProfileSetupRequest(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    @field:NotBlank(message = "닉네임은 필수입니다.")
    @field:Pattern(
        regexp = "^[가-힣a-zA-Z0-9]{2,20}$",
        message = "닉네임은 2-20자의 한글, 영문, 숫자만 사용 가능합니다."
    )
    val nickname: String,


    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val profileImageUrl: String?
)

