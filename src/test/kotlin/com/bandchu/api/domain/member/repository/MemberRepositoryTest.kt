package com.bandchu.api.domain.member.repository

import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.model.Role
import org.junit.jupiter.api.Test
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MemberRepositoryTest {
    private val memberRepository = mockk<MemberRepository>()

    @Test
    fun `새로운 회원을 저장하면 ID가 할당된다`() {
        // given
        val member = Member(
            email = "test@example.com",
            password = "password123",
            nickname = "테스트유저",
            role = Role.FAN
        )

        val savedMember = member.copy(
            id = 1L,
            createdAt = LocalDateTime(2024, Month.JANUARY, 1, 0, 0, 0)
        )

        every { memberRepository.save(member) } returns savedMember

        // when
        val result = memberRepository.save(member)

        // then
        assertNotNull(result.id)
        assertEquals("test@example.com", result.email)
        assertEquals("password123", result.password)
        assertEquals("테스트유저", result.nickname)
        assertEquals(Role.FAN, result.role)
        assertNotNull(result.createdAt)
    }

    @Test
    fun `존재하는 이메일을 조회하면 true를 반환한다`() {
        // given
        val email = "exists@example.com"

        every { memberRepository.existsByEmail(email) } returns true

        // when
        val result = memberRepository.existsByEmail(email)

        // then
        assertTrue(result)
    }
}