package com.bandchu.api.domain.member.controller

import com.bandchu.api.domain.member.dto.LoginRequest
import com.bandchu.api.domain.member.dto.LoginResponse
import com.bandchu.api.domain.member.dto.RefreshTokenRequest
import com.bandchu.api.domain.member.dto.RefreshTokenResponse
import com.bandchu.api.domain.member.dto.SignupRequest
import com.bandchu.api.domain.member.dto.SignupResponse
import com.bandchu.api.domain.member.service.MemberService
import com.bandchu.api.global.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.time.ZoneOffset

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService
) {

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<ApiResponse<SignupResponse>> {
        val savedMember = memberService.signup(request)
        
        val memberId = savedMember.id ?: throw IllegalStateException("회원 저장 후 ID가 없습니다.")
        
        val response = SignupResponse(
            memberId = memberId,
            email = savedMember.email,
            nickname = savedMember.nickname,
            role = savedMember.role,
            createdAt = savedMember.createdAt?.let { localDateTime ->
                java.time.LocalDateTime.of(
                    localDateTime.year,
                    java.time.Month.valueOf(localDateTime.month.name),
                    localDateTime.day,
                    localDateTime.hour,
                    localDateTime.minute,
                    localDateTime.second,
                    localDateTime.nanosecond
                ).atOffset(ZoneOffset.UTC)
            } ?: OffsetDateTime.now(ZoneOffset.UTC)
        )
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "회원 가입이 완료되었습니다."))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val loginResult = memberService.login(request)
        
        val response = LoginResponse(
            accessToken = loginResult.accessToken,
            refreshToken = loginResult.refreshToken,
            memberId = loginResult.memberId,
            nickname = loginResult.nickname
        )
        
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(response, "로그인 되었습니다."))
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<ApiResponse<Unit>> {
        // JWT 필터에서 이미 인증 검증을 완료했으므로, 여기서는 단순히 성공 응답만 반환
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(Unit, "로그아웃되었습니다."))
    }

    @PostMapping("/token/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<ApiResponse<RefreshTokenResponse>> {
        val tokenPair = memberService.refreshToken(request.refreshToken)
        
        val response = RefreshTokenResponse(
            accessToken = tokenPair.accessToken,
            refreshToken = tokenPair.refreshToken
        )
        
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(response, "토큰이 성공적으로 재발급되었습니다."))
    }
}

