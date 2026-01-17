package com.bandchu.api.domain.member.model

import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MemberTest {

    @Test
    fun `올바른 비밀번호로 검증하면 예외가 발생하지 않는다`() {
        // given
        val member = Member(
            email = "test@example.com",
            password = "password123",
            nickname = "테스트유저",
            role = Role.FAN
        )

        // when & then
        member.verifyPassword("password123") // 예외 발생하지 않음
    }

    @Test
    fun `잘못된 비밀번호로 검증하면 USER_INVALID_CREDENTIAL 예외를 발생시킨다`() {
        // given
        val member = Member(
            email = "test@example.com",
            password = "password123",
            nickname = "테스트유저",
            role = Role.FAN
        )

        // when & then
        try {
            member.verifyPassword("wrongpassword")
            assert(false) { "Expected BusinessException to be thrown" }
        } catch (e: BusinessException) {
            assertEquals(ErrorCode.USER_INVALID_CREDENTIAL, e.errorCode)
        }
    }

    @Test
    fun `일반 회원가입용 회원을 생성하면 프로필 완료 상태로 생성된다`() {
        // when
        val member = Member.createForSignup(
            email = "test@example.com",
            password = "password123",
            nickname = "테스트유저",
            role = Role.FAN
        )

        // then
        assertEquals("test@example.com", member.email)
        assertEquals("password123", member.password)
        assertEquals("테스트유저", member.nickname)
        assertEquals(Role.FAN, member.role)
        assertTrue(member.isProfileCompleted)
        assertNull(member.googleId)
        assertNull(member.profileImageUrl)
    }

    @Test
    fun `ARTIST 역할로도 생성할 수 있다`() {
        // when
        val member = Member.createForSignup(
            email = "artist@example.com",
            password = "password123",
            nickname = "아티스트",
            role = Role.ARTIST
        )

        // then
        assertEquals(Role.ARTIST, member.role)
        assertTrue(member.isProfileCompleted)
    }

    @Test
    fun `OAuth 회원가입용 회원을 생성하면 프로필 미완료 상태로 생성되고 기본 역할은 FAN이다`() {
        // when
        val member = Member.createForOAuth(
            email = "oauth@example.com",
            nickname = "구글유저",
            googleId = "google-id-123"
        )

        // then
        assertEquals("oauth@example.com", member.email)
        assertEquals("", member.password) // OAuth 회원은 비밀번호 없음
        assertEquals("구글유저", member.nickname)
        assertEquals(Role.FAN, member.role) // 기본 역할은 FAN
        assertEquals("google-id-123", member.googleId)
        assertTrue(!member.isProfileCompleted) // 프로필 미완료 상태
        assertNull(member.profileImageUrl)
    }
}