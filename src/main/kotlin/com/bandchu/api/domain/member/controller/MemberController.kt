package com.bandchu.api.domain.member.controller

import com.bandchu.api.domain.member.dto.GoogleOAuthRequest
import com.bandchu.api.domain.member.dto.GoogleOAuthResponse
import com.bandchu.api.domain.member.dto.LoginRequest
import com.bandchu.api.domain.member.dto.LoginResponse
import com.bandchu.api.domain.member.dto.OAuthLinkRequest
import com.bandchu.api.domain.member.dto.OAuthLinkResponse
import com.bandchu.api.domain.member.dto.OAuthVerifyRequest
import com.bandchu.api.domain.member.dto.OAuthVerifyResponse
import com.bandchu.api.domain.member.dto.ProfileSetupRequest
import com.bandchu.api.domain.member.dto.ProfileSetupResponse
import com.bandchu.api.domain.member.dto.RefreshTokenRequest
import com.bandchu.api.domain.member.dto.RefreshTokenResponse
import com.bandchu.api.domain.member.dto.SignupRequest
import com.bandchu.api.domain.member.dto.SignupResponse
import com.bandchu.api.domain.member.service.MemberService
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import com.bandchu.api.global.response.ApiResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.time.ZoneOffset

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<ApiResponse<SignupResponse>> {
        val savedMember = memberService.signup(request)
        
        val memberId = savedMember.id ?: run {
            log.error("Critical: Member ID is null after save. Email: ${savedMember.email}")
            throw IllegalStateException("회원 저장 후 ID가 없습니다.")
        }
        
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

    @PostMapping("/oauth/google")
    fun googleLogin(@Valid @RequestBody request: GoogleOAuthRequest): ResponseEntity<ApiResponse<GoogleOAuthResponse>> {
        val googleOAuthResult = memberService.googleLogin(request.idToken)
        
        val response = GoogleOAuthResponse(
            accessToken = googleOAuthResult.accessToken,
            refreshToken = googleOAuthResult.refreshToken,
            isNewMember = googleOAuthResult.isNewMember,
            memberId = googleOAuthResult.memberId,
            nickname = googleOAuthResult.nickname
        )
        
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(response, "구글 로그인에 성공했습니다."))
    }

    @PostMapping("/oauth/verify")
    fun verifyOAuth(@Valid @RequestBody request: OAuthVerifyRequest): ResponseEntity<ApiResponse<OAuthVerifyResponse>> {
        val oauthVerifyResult = memberService.verifyOAuth(request.provider, request.token)
        
        val response = OAuthVerifyResponse(
            accessToken = oauthVerifyResult.accessToken,
            refreshToken = oauthVerifyResult.refreshToken,
            memberId = oauthVerifyResult.memberId
        )
        
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(response, "소셜 인증 검증에 성공했습니다."))
    }

    @PostMapping("/me/oauth/link")
    fun linkOAuth(@Valid @RequestBody request: OAuthLinkRequest): ResponseEntity<ApiResponse<OAuthLinkResponse>> {
        // SecurityContext에서 인증된 회원 ID 가져오기
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
            ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
        val memberId = authentication.principal as Long

        val oauthLinkResult = memberService.linkOAuth(memberId, request.provider, request.token)
        
        val response = OAuthLinkResponse(
            linkedProvider = oauthLinkResult.linkedProvider
        )
        
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(response, "소셜 계정이 연결되었습니다."))
    }

    @DeleteMapping("/me")
    fun deleteMember(): ResponseEntity<ApiResponse<Unit?>> {
        // SecurityContext에서 인증된 회원 ID 가져오기
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
            ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
        val memberId = authentication.principal as Long

        memberService.deleteMember(memberId)
        
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse<Unit?>(true, null, "회원 탈퇴가 완료되었습니다."))
    }

    @PatchMapping("/me/profile/setup")
    fun setupProfile(@Valid @RequestBody request: ProfileSetupRequest): ResponseEntity<ApiResponse<ProfileSetupResponse>> {
        // SecurityContext에서 인증된 회원 ID 가져오기
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
            ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
        val memberId = authentication.principal as Long

        val updatedMember = memberService.setupProfile(memberId, request.nickname, request.profileImageUrl)
        
        val memberIdValue = updatedMember.id ?: run {
            log.error("Critical: Member ID is null after profile setup. Email: ${updatedMember.email}")
            throw IllegalStateException("회원 ID가 없습니다.")
        }
        
        val response = ProfileSetupResponse(
            memberId = memberIdValue,
            nickname = updatedMember.nickname,
            profileImageUrl = updatedMember.profileImageUrl
        )
        
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(response, "프로필 초기 설정이 완료되었습니다."))
    }
}

