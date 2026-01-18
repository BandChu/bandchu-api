package com.bandchu.api.domain.member.dto

import com.bandchu.api.domain.member.model.Role
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SignupRequest(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    @field:NotBlank(message = "이메일은 필수입니다.")
    val email: String,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    @field:NotBlank(message = "닉네임은 필수입니다.")
    val nickname: String,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    @field:NotNull(message = "역할은 필수입니다.")
    val role: Role
)

