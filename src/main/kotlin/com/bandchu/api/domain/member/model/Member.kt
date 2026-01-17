package com.bandchu.api.domain.member.model

import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import kotlinx.datetime.LocalDateTime

data class Member(
    val id: Long? = null,
    val email: String,
    val password: String,
    val nickname: String,
    val role: Role,
    val googleId: String? = null,
    val profileImageUrl: String? = null,
    val isProfileCompleted: Boolean = false,
    val createdAt: LocalDateTime? = null
) {
    /**
     * 비밀번호 검증
     * @param rawPassword 입력된 비밀번호 (평문)
     * @throws BusinessException 비밀번호가 일치하지 않으면 USER_INVALID_CREDENTIAL 예외 발생
     * TODO: 비밀번호 암호화 추가 후 BCrypt 등으로 비교하도록 변경 필요
     */
    fun verifyPassword(rawPassword: String) {
        if (this.password != rawPassword) {
            throw BusinessException(ErrorCode.USER_INVALID_CREDENTIAL)
        }
    }

    companion object {
        /**
         * 일반 회원가입용 회원 생성
         * 프로필 정보를 이미 받으므로 완료 상태로 설정
         */
        fun createForSignup(
            email: String,
            password: String,
            nickname: String,
            role: Role
        ): Member {
            return Member(
                email = email,
                password = password, // TODO: 비밀번호 암호화 추가 필요
                nickname = nickname,
                role = role,
                isProfileCompleted = true
            )
        }

        /**
         * OAuth 회원가입용 회원 생성
         * 프로필 설정 전까지 미완료 상태로 생성하고, 기본 역할은 FAN
         */
        fun createForOAuth(
            email: String,
            nickname: String,
            googleId: String
        ): Member {
            return Member(
                email = email,
                password = "", // OAuth 회원은 비밀번호 없음
                nickname = nickname,
                role = Role.FAN, // 기본 역할은 FAN
                googleId = googleId,
                isProfileCompleted = false // OAuth 회원가입은 프로필 설정 전까지 미완료 상태
            )
        }
    }
}