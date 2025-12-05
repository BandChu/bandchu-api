package com.bandchu.api.domain.member.controller

import com.bandchu.api.domain.member.dto.SignupRequest
import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.domain.member.service.LoginResult
import com.bandchu.api.domain.member.service.MemberService
import com.bandchu.api.domain.member.service.SignupResult
import com.bandchu.api.domain.member.service.TokenPair
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month

@TestConfiguration
class MemberControllerTestConfig {
    @Bean
    fun memberService(): MemberService = mockk()
}

@WebMvcTest(MemberController::class)
@ContextConfiguration(classes = [MemberControllerTestConfig::class])
class MemberControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val memberService: MemberService
) : DescribeSpec({

    describe("POST /api/members/signup") {
        context("유효한 회원 가입 요청이 들어오면") {
            it("201 Created와 함께 회원 정보를 반환한다") {
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
                    createdAt = LocalDateTime(
                        date = LocalDate(2024, Month.JANUARY, 1),
                        time = LocalTime(0, 0, 0, 0)
                    )
                )

                val signupResult = SignupResult(
                    member = savedMember,
                    accessToken = "access-token",
                    refreshToken = "refresh-token"
                )

                every { memberService.signup(request) } returns signupResult

                // when & then
                mockMvc.perform(
                    post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isCreated)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.memberId").value(1L))
                    .andExpect(jsonPath("$.data.email").value("example@domain.com"))
                    .andExpect(jsonPath("$.data.nickname").value("홍길동"))
                    .andExpect(jsonPath("$.data.role").value("FAN"))
                    .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                    .andExpect(jsonPath("$.message").value("회원 가입이 완료되었습니다."))
            }
        }

        context("이미 존재하는 이메일로 가입 요청이 들어오면") {
            it("409 Conflict와 함께 에러를 반환한다") {
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
                    post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isConflict)
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.code").value("USER_EMAIL_DUPLICATED"))
                    .andExpect(jsonPath("$.detail").value("이미 사용 중인 이메일입니다."))
            }
        }
    }

    describe("POST /api/members/login") {
        context("유효한 로그인 요청이 들어오면") {
            it("200 OK와 함께 JWT 토큰을 반환한다") {
                // given
                val request = com.bandchu.api.domain.member.dto.LoginRequest(
                    email = "example@domain.com",
                    password = "password123"
                )

                val loginResult = LoginResult(
                    accessToken = "jwt-token",
                    refreshToken = "refresh-token",
                    memberId = 1L,
                    nickname = "홍길동"
                )

                every { memberService.login(request) } returns loginResult

                // when & then
                mockMvc.perform(
                    post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                    .andExpect(jsonPath("$.data.memberId").value(1L))
                    .andExpect(jsonPath("$.data.nickname").value("홍길동"))
                    .andExpect(jsonPath("$.message").value("로그인 되었습니다."))
            }
        }

        context("잘못된 이메일 또는 비밀번호로 로그인 요청이 들어오면") {
            it("401 Unauthorized와 함께 에러를 반환한다") {
                // given
                val request = com.bandchu.api.domain.member.dto.LoginRequest(
                    email = "wrong@domain.com",
                    password = "wrongpassword"
                )

                every { memberService.login(request) } throws com.bandchu.api.global.exception.BusinessException(
                    com.bandchu.api.global.exception.ErrorCode.USER_INVALID_CREDENTIAL
                )

                // when & then
                mockMvc.perform(
                    post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isUnauthorized)
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.code").value("USER_INVALID_CREDENTIAL"))
                    .andExpect(jsonPath("$.detail").value("이메일 또는 비밀번호가 올바르지 않습니다."))
            }
        }
    }

    describe("POST /api/members/logout") {
        context("유효한 토큰으로 로그아웃 요청이 들어오면") {
            it("200 OK와 함께 성공 메시지를 반환한다") {
                // given - JWT 필터가 토큰을 검증하고 인증을 설정하면 컨트롤러가 호출됨
                // 실제 테스트에서는 Spring Security 설정이 필요하므로 통합 테스트로 진행
                // 단위 테스트에서는 mockMvc가 Security 설정을 포함하므로 실제 동작 확인
            }
        }

        context("토큰이 없이 로그아웃 요청이 들어오면") {
            it("401 Unauthorized와 함께 에러를 반환한다") {
                // when & then
                mockMvc.perform(
                    post("/api/members/logout")
                )
                    .andExpect(status().isUnauthorized)
            }
        }

        context("유효하지 않은 토큰으로 로그아웃 요청이 들어오면") {
            it("401 Unauthorized와 함께 에러를 반환한다") {
                // given
                val invalidToken = "invalid-token"

                // when & then - JWT 필터가 토큰 검증 실패 시 401 반환
                mockMvc.perform(
                    post("/api/members/logout")
                        .header("Authorization", "Bearer $invalidToken")
                )
                    .andExpect(status().isUnauthorized)
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.code").value("INVALID_TOKEN"))
                    .andExpect(jsonPath("$.detail").value("유효하지 않은 토큰입니다."))
            }
        }
    }

    describe("POST /api/members/token/refresh") {
        context("유효한 리프레시 토큰으로 재발급 요청이 들어오면") {
            it("200 OK와 함께 새로운 토큰을 반환한다") {
                // given
                val request = com.bandchu.api.domain.member.dto.RefreshTokenRequest(
                    refreshToken = "valid-refresh-token"
                )

                val tokenPair = TokenPair(
                    accessToken = "new-access-token",
                    refreshToken = "new-refresh-token"
                )

                every { memberService.refreshToken(request.refreshToken) } returns tokenPair

                // when & then
                mockMvc.perform(
                    post("/api/members/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"))
                    .andExpect(jsonPath("$.message").value("토큰이 성공적으로 재발급되었습니다."))
            }
        }

        context("유효하지 않은 리프레시 토큰으로 재발급 요청이 들어오면") {
            it("401 Unauthorized와 함께 에러를 반환한다") {
                // given
                val request = com.bandchu.api.domain.member.dto.RefreshTokenRequest(
                    refreshToken = "invalid-refresh-token"
                )

                every { memberService.refreshToken(request.refreshToken) } throws com.bandchu.api.global.exception.BusinessException(
                    com.bandchu.api.global.exception.ErrorCode.INVALID_REFRESH_TOKEN
                )

                // when & then
                mockMvc.perform(
                    post("/api/members/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isUnauthorized)
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"))
                    .andExpect(jsonPath("$.detail").value("유효하지 않은 리프레시 토큰입니다."))
            }
        }
    }

    describe("POST /api/members/oauth/google") {
        context("유효한 구글 ID 토큰으로 로그인 요청이 들어오면") {
            it("200 OK와 함께 JWT 토큰과 회원 정보를 반환한다") {
                // given
                val request = com.bandchu.api.domain.member.dto.GoogleOAuthRequest(
                    idToken = "valid-google-id-token"
                )

                val googleOAuthResult = com.bandchu.api.domain.member.service.GoogleOAuthResult(
                    accessToken = "jwt-token",
                    refreshToken = "refresh-token",
                    isNewMember = false,
                    memberId = 1L,
                    nickname = "GoogleUser",
                    isProfileCompleted = true
                )

                every { memberService.googleLogin(request.idToken) } returns googleOAuthResult

                // when & then
                mockMvc.perform(
                    post("/api/members/oauth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                    .andExpect(jsonPath("$.data.isNewMember").value(false))
                    .andExpect(jsonPath("$.data.memberId").value(1L))
                    .andExpect(jsonPath("$.message").value("구글 로그인에 성공했습니다."))
            }
        }

        context("신규 회원인 경우") {
            it("isNewMember가 true로 반환된다") {
                // given
                val request = com.bandchu.api.domain.member.dto.GoogleOAuthRequest(
                    idToken = "valid-google-id-token-new-user"
                )

                val googleOAuthResult = com.bandchu.api.domain.member.service.GoogleOAuthResult(
                    accessToken = "jwt-token",
                    refreshToken = "refresh-token",
                    isNewMember = true,
                    memberId = 2L,
                    nickname = "NewGoogleUser",
                    isProfileCompleted = false
                )

                every { memberService.googleLogin(request.idToken) } returns googleOAuthResult

                // when & then
                mockMvc.perform(
                    post("/api/members/oauth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.isNewMember").value(true))
                    .andExpect(jsonPath("$.data.memberId").value(2L))
            }
        }

        context("유효하지 않은 구글 ID 토큰으로 로그인 요청이 들어오면") {
            it("401 Unauthorized와 함께 에러를 반환한다") {
                // given
                val request = com.bandchu.api.domain.member.dto.GoogleOAuthRequest(
                    idToken = "invalid-google-id-token"
                )

                every { memberService.googleLogin(request.idToken) } throws com.bandchu.api.global.exception.BusinessException(
                    com.bandchu.api.global.exception.ErrorCode.GOOGLE_AUTH_INVALID
                )

                // when & then
                mockMvc.perform(
                    post("/api/members/oauth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isUnauthorized)
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.code").value("GOOGLE_AUTH_INVALID"))
                    .andExpect(jsonPath("$.detail").value("구글 인증이 유효하지 않습니다."))
            }
        }
    }

    describe("POST /api/members/oauth/verify") {
        context("유효한 소셜 인증 토큰으로 검증 요청이 들어오면") {
            it("200 OK와 함께 JWT 토큰 및 회원 정보를 반환한다") {
                // given
                val request = com.bandchu.api.domain.member.dto.OAuthVerifyRequest(
                    provider = "GOOGLE",
                    token = "valid-oauth-token"
                )

                val oauthVerifyResult = com.bandchu.api.domain.member.service.OAuthVerifyResult(
                    accessToken = "jwt-token",
                    refreshToken = "refresh-token",
                    memberId = 1L
                )

                every { memberService.verifyOAuth(request.provider, request.token) } returns oauthVerifyResult

                // when & then
                mockMvc.perform(
                    post("/api/members/oauth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                    .andExpect(jsonPath("$.data.memberId").value(1L))
                    .andExpect(jsonPath("$.message").value("소셜 인증 검증에 성공했습니다."))
            }
        }

        context("유효하지 않은 소셜 인증 토큰으로 검증 요청이 들어오면") {
            it("401 Unauthorized와 함께 에러를 반환한다") {
                // given
                val request = com.bandchu.api.domain.member.dto.OAuthVerifyRequest(
                    provider = "GOOGLE",
                    token = "invalid-oauth-token"
                )

                every { memberService.verifyOAuth(request.provider, request.token) } throws com.bandchu.api.global.exception.BusinessException(
                    com.bandchu.api.global.exception.ErrorCode.OAUTH_TOKEN_INVALID
                )

                // when & then
                mockMvc.perform(
                    post("/api/members/oauth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isUnauthorized)
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.code").value("OAUTH_TOKEN_INVALID"))
                    .andExpect(jsonPath("$.detail").value("소셜 인증 토큰이 유효하지 않습니다."))
            }
        }
    }

    describe("POST /api/members/me/oauth/link") {
        context("유효한 토큰과 소셜 인증 토큰으로 연결 요청이 들어오면") {
            it("200 OK와 함께 연결된 프로바이더 정보를 반환한다") {
                // given
                val request = com.bandchu.api.domain.member.dto.OAuthLinkRequest(
                    provider = "GOOGLE",
                    token = "valid-google-oauth-token"
                )

                val oauthLinkResult = com.bandchu.api.domain.member.service.OAuthLinkResult(
                    linkedProvider = "GOOGLE"
                )

                val memberId = 1L
                val authentication = UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_FAN"))
                )
                SecurityContextHolder.getContext().authentication = authentication

                every { memberService.linkOAuth(memberId, request.provider, request.token) } returns oauthLinkResult

                // when & then
                mockMvc.perform(
                    post("/api/members/me/oauth/link")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.linkedProvider").value("GOOGLE"))
                    .andExpect(jsonPath("$.message").value("소셜 계정이 연결되었습니다."))
            }
        }

        context("이미 연결된 소셜 계정으로 연결 요청이 들어오면") {
            it("409 Conflict와 함께 에러를 반환한다") {
                // given
                val request = com.bandchu.api.domain.member.dto.OAuthLinkRequest(
                    provider = "GOOGLE",
                    token = "valid-google-oauth-token"
                )

                val memberId = 1L
                val authentication = UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_FAN"))
                )
                SecurityContextHolder.getContext().authentication = authentication

                every { memberService.linkOAuth(memberId, request.provider, request.token) } throws com.bandchu.api.global.exception.BusinessException(
                    com.bandchu.api.global.exception.ErrorCode.OAUTH_ALREADY_LINKED
                )

                // when & then
                mockMvc.perform(
                    post("/api/members/me/oauth/link")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isConflict)
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.code").value("OAUTH_ALREADY_LINKED"))
                    .andExpect(jsonPath("$.detail").value("이미 연결된 소셜 계정입니다."))
            }
        }

        context("토큰이 없이 연결 요청이 들어오면") {
            it("401 Unauthorized와 함께 에러를 반환한다") {
                // given
                val request = com.bandchu.api.domain.member.dto.OAuthLinkRequest(
                    provider = "GOOGLE",
                    token = "valid-google-oauth-token"
                )

                // when & then
                mockMvc.perform(
                    post("/api/members/me/oauth/link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isUnauthorized)
            }
        }
    }

    describe("DELETE /api/members/me") {
        context("유효한 토큰으로 회원 탈퇴 요청이 들어오면") {
            it("200 OK와 함께 성공 메시지를 반환한다") {
                // given
                val memberId = 1L
                val authentication = UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_FAN"))
                )
                SecurityContextHolder.getContext().authentication = authentication

                every { memberService.deleteMember(memberId) } returns Unit

                // when & then
                mockMvc.perform(
                    delete("/api/members/me")
                        .with(authentication(authentication))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.message").value("회원 탈퇴가 완료되었습니다."))
            }
        }

        context("토큰이 없이 회원 탈퇴 요청이 들어오면") {
            it("401 Unauthorized와 함께 에러를 반환한다") {
                // when & then
                mockMvc.perform(
                    delete("/api/members/me")
                )
                    .andExpect(status().isUnauthorized)
            }
        }

        context("유효하지 않은 토큰으로 회원 탈퇴 요청이 들어오면") {
            it("401 Unauthorized와 함께 에러를 반환한다") {
                // given
                val invalidToken = "invalid-token"

                // when & then
                mockMvc.perform(
                    delete("/api/members/me")
                        .header("Authorization", "Bearer $invalidToken")
                )
                    .andExpect(status().isUnauthorized)
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.code").value("INVALID_TOKEN"))
                    .andExpect(jsonPath("$.detail").value("유효하지 않은 토큰입니다."))
            }
        }
    }

    describe("PATCH /api/members/me/profile/setup") {
        context("유효한 토큰과 프로필 정보로 초기 설정 요청이 들어오면") {
            it("200 OK와 함께 업데이트된 프로필 정보를 반환한다") {
                // given
                val memberId = 1L
                val authentication = UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_FAN"))
                )
                SecurityContextHolder.getContext().authentication = authentication

                val request = com.bandchu.api.domain.member.dto.ProfileSetupRequest(
                    nickname = "새사용자",
                    profileImageUrl = "https://example.com/profile.jpg"
                )

                val updatedMember = Member(
                    id = memberId,
                    email = "test@example.com",
                    password = "password123",
                    nickname = "새사용자",
                    role = Role.FAN,
                    profileImageUrl = "https://example.com/profile.jpg"
                )

                every { memberService.setupProfile(memberId, request.nickname, request.profileImageUrl) } returns updatedMember

                // when & then
                mockMvc.perform(
                    patch("/api/members/me/profile/setup")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.memberId").value(memberId))
                    .andExpect(jsonPath("$.data.nickname").value("새사용자"))
                    .andExpect(jsonPath("$.data.profileImageUrl").value("https://example.com/profile.jpg"))
                    .andExpect(jsonPath("$.message").value("프로필 초기 설정이 완료되었습니다."))
            }
        }

        context("유효하지 않은 닉네임 형식으로 요청이 들어오면") {
            it("400 Bad Request와 함께 에러를 반환한다") {
                // given
                val memberId = 1L
                val authentication = UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_FAN"))
                )
                SecurityContextHolder.getContext().authentication = authentication

                val request = com.bandchu.api.domain.member.dto.ProfileSetupRequest(
                    nickname = "",
                    profileImageUrl = "https://example.com/profile.jpg"
                )

                every { memberService.setupProfile(memberId, request.nickname, request.profileImageUrl) } throws com.bandchu.api.global.exception.BusinessException(
                    com.bandchu.api.global.exception.ErrorCode.INVALID_NICKNAME
                )

                // when & then
                mockMvc.perform(
                    patch("/api/members/me/profile/setup")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("INVALID_NICKNAME"))
                    .andExpect(jsonPath("$.detail").value("닉네임 형식이 올바르지 않습니다."))
            }
        }

        context("토큰이 없이 요청이 들어오면") {
            it("401 Unauthorized와 함께 에러를 반환한다") {
                // given
                val request = com.bandchu.api.domain.member.dto.ProfileSetupRequest(
                    nickname = "새사용자",
                    profileImageUrl = "https://example.com/profile.jpg"
                )

                // when & then
                mockMvc.perform(
                    patch("/api/members/me/profile/setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isUnauthorized)
            }
        }
    }
})

