package com.bandchu.api.domain.member.controller

import com.bandchu.api.domain.member.dto.SignupRequest
import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.domain.member.service.MemberService
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import com.bandchu.api.global.exception.GlobalExceptionHandler

@TestConfiguration
class MemberControllerTestConfig {
    @Bean
    fun memberService(): MemberService = mockk()
}

@WebMvcTest(MemberController::class, excludeAutoConfiguration = [SecurityAutoConfiguration::class])
@ContextConfiguration(classes = [MemberController::class, MemberControllerTestConfig::class, GlobalExceptionHandler::class])
class MemberControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val memberService: MemberService
) {

    @Test
    fun `유효한 회원 가입 요청이 들어오면 201 Created와 함께 회원 정보를 반환한다`() {
        // given
        val request = SignupRequest(
            email = "example@domain.com",
            password = "password123",
            nickname = "홍길동",
            role = Role.FAN
        )

        val savedMember = Member(
            id = 1L,
            email = "example@domain.com",
            password = "password123",
            nickname = "홍길동",
            role = Role.FAN,
            createdAt = kotlinx.datetime.LocalDateTime(
                year = 2024,
                month = kotlinx.datetime.Month.JANUARY,
                dayOfMonth = 1,
                hour = 0,
                minute = 0,
                second = 0,
                nanosecond = 0
            )
        )

        every { memberService.signup(request) } returns savedMember

        // when & then
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/members/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated)
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.memberId").value(1))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.email").value("example@domain.com"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.nickname").value("홍길동"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.role").value("FAN"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.message").value("회원 가입이 완료되었습니다."))
    }

    @Test
    fun `이미 존재하는 이메일로 가입 요청이 들어오면 409 Conflict와 함께 에러를 반환한다`() {
        // given
        val request = SignupRequest(
            email = "duplicate@domain.com",
            password = "password123",
            nickname = "홍길동",
            role = Role.FAN
        )

        every { memberService.signup(request) } throws com.bandchu.api.global.exception.BusinessException(
            com.bandchu.api.global.exception.ErrorCode.USER_EMAIL_DUPLICATED
        )

        // when & then
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/members/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isConflict)
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status").value(409))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code").value("USER_EMAIL_DUPLICATED"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.detail").value("이미 사용 중인 이메일입니다."))
    }
}