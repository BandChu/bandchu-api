package com.bandchu.api.domain.member.dto

import com.bandchu.api.domain.member.model.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SignupRequest(
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    @field:NotBlank(message = "이메일은 필수입니다.")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String,

    @field:NotBlank(message = "닉네임은 필수입니다.")
    val nickname: String,

    @field:NotNull(message = "역할은 필수입니다.")
    val role: Role
)

