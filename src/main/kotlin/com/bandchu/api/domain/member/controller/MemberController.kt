package com.bandchu.api.domain.member.controller

import com.bandchu.api.domain.member.dto.GoogleOAuthRequest
import com.bandchu.api.domain.member.dto.GoogleOAuthResponse
import com.bandchu.api.domain.member.dto.LoginRequest
import com.bandchu.api.domain.member.dto.LoginResponse
import com.bandchu.api.domain.member.dto.OAuthLinkRequest
import com.bandchu.api.domain.member.dto.OAuthLinkResponse
import com.bandchu.api.domain.member.dto.OAuthVerifyRequest
import com.bandchu.api.domain.member.dto.OAuthVerifyResponse
import com.bandchu.api.domain.member.dto.MemberInfoResponse
import com.bandchu.api.domain.member.dto.ProfileSetupRequest
import com.bandchu.api.domain.member.dto.ProfileSetupResponse
import com.bandchu.api.domain.member.dto.RefreshTokenRequest
import com.bandchu.api.domain.member.dto.RefreshTokenResponse
import com.bandchu.api.domain.member.dto.RoleUpdateRequest
import com.bandchu.api.domain.member.dto.RoleUpdateResponse
import com.bandchu.api.domain.member.dto.SignupRequest
import com.bandchu.api.domain.member.dto.SignupResponse
import com.bandchu.api.domain.member.dto.SocialLoginResponse
import com.bandchu.api.domain.member.dto.SocialOAuthRequest
import com.bandchu.api.domain.member.service.KakaoOAuthService
import com.bandchu.api.domain.member.service.MemberService
import com.bandchu.api.domain.member.service.NaverOAuthService
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import com.bandchu.api.global.response.ApiResponse
import com.bandchu.api.global.util.toOffsetDateTime
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Member", description = "회원 관련 API")
class MemberController(
    private val memberService: MemberService,
    private val naverOAuthService: NaverOAuthService,
    private val kakaoOAuthService: KakaoOAuthService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Operation(summary = "회원가입", description = "일반 회원가입 api입니다")
    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<ApiResponse<SignupResponse>> {
        val signupResult = memberService.signup(request)
        
        val member = signupResult.member
        val memberId = member.id ?: run {
            log.error("Critical: Member ID is null after save. Email: ${member.email}")
            throw IllegalStateException("회원 저장 후 ID가 없습니다.")
        }
        
        val response = SignupResponse(
            memberId = memberId,
            email = member.email,
            nickname = member.nickname,
            role = member.role,
            accessToken = signupResult.accessToken,
            refreshToken = signupResult.refreshToken,
            createdAt = member.createdAt?.toOffsetDateTime() ?: OffsetDateTime.now(ZoneOffset.UTC)
        )
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "회원 가입이 완료되었습니다."))
    }
    @Operation(summary = "로그인", description = "일반 로그인 기능입니다.")
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
    @Operation(summary = "로그아웃", description = "로그아웃합니다.")
    @PostMapping("/logout")
    fun logout(): ResponseEntity<ApiResponse<Unit>> {
        // JWT 필터에서 이미 인증 검증을 완료했으므로, 여기서는 단순히 성공 응답만 반환
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(Unit, "로그아웃되었습니다."))
    }
    @Operation(summary = "토큰 리프레시", description = "리프레시 토큰 기반으로 access token을 재발급합니다.")
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

    @GetMapping("/oauth/kakao/callback")
    fun kakaoCallback(
        @RequestParam code: String
    ): ResponseEntity<ApiResponse<SocialLoginResponse>> {
        // 1. 카카오로부터 진짜 신분증(AccessToken) 받아오기
        val accessToken = kakaoOAuthService.getAccessToken(code)

        // 2. 그 신분증으로 우리 서비스 로그인 처리
        val result = memberService.socialLogin("KAKAO", accessToken)

        val response = SocialLoginResponse(
            accessToken = result.accessToken,
            refreshToken = result.refreshToken,
            isNewMember = result.isNewMember,
            memberId = result.memberId,
            nickname = result.nickname
        )

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(response, "카카오 로그인 성공"))
    }

    @GetMapping("/oauth/naver/callback")
    fun naverCallback(
        @RequestParam code: String,
        @RequestParam state: String
    ): ResponseEntity<ApiResponse<SocialLoginResponse>> {
        // 1. 받은 code로 네이버에 Access Token 요청 (보통 서비스에서 처리)
        // 2. 받은 Access Token으로 socialLogin(provider = "NAVER", token = accessToken) 호출
        // 3. 최종 결과 반환
        // 1. 응답용 DTO 생성
        val accessToken = naverOAuthService.getAccessToken(code, state)
        val result = memberService.socialLogin("NAVER", accessToken)
        val response = SocialLoginResponse(
            accessToken = result.accessToken,
            refreshToken = result.refreshToken,
            isNewMember = result.isNewMember,
            memberId = result.memberId,
            nickname = result.nickname
        )

        // 2. data 자리에 response를 넣어줍니다!
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(response, "네이버 로그인 성공"))
    }


    @Operation(summary = "소셜 로그인", description = "OAuth(Google, Naver, Kakao)를 통해 로그인/가입합니다.")
    @PostMapping("/oauth/{provider}")
    fun socialLogin(
        @PathVariable provider: String,
        @Valid @RequestBody request: SocialOAuthRequest // 통합 DTO 사용
    ): ResponseEntity<ApiResponse<SocialLoginResponse>> {

        // 서비스 호출 (통합된 socialLogin 메서드 활용)
        val result = memberService.socialLogin(provider, request.token)

        val response = SocialLoginResponse(
            accessToken = result.accessToken,
            refreshToken = result.refreshToken,
            isNewMember = result.isNewMember,
            memberId = result.memberId,
            nickname = result.nickname
        )

        val message = if (result.isNewMember) "회원 유형을 선택해주세요." else "${provider.uppercase()} 로그인에 성공했습니다."

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(response, message))
    }


        @Operation(summary = "oauth verify", description = "oauth 맞는지 확인하는 것입니다. ")
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
    @Operation(summary = "앨범 일부 삭제", description = "앨범을 앨범 ID를 통해 삭제합니다")
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
    @Operation(summary = "앨범 일부 삭제", description = "앨범을 앨범 ID를 통해 삭제합니다")
    @GetMapping("/me")
    fun getMemberInfo(): ResponseEntity<ApiResponse<MemberInfoResponse>> {
        // SecurityContext에서 인증된 회원 ID 가져오기
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
            ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
        val memberId = authentication.principal as Long

        val member = memberService.getMemberInfo(memberId)
        
        val memberIdValue = member.id ?: run {
            log.error("Critical: Member ID is null. Email: ${member.email}")
            throw IllegalStateException("회원 ID가 없습니다.")
        }
        
        val response = MemberInfoResponse(
            memberId = memberIdValue,
            email = member.email,
            nickname = member.nickname,
            role = member.role,
            profileImageUrl = member.profileImageUrl
        )
        
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(response, "사용자 정보 조회에 성공했습니다."))
    }
    @Operation(summary = "회원 삭제", description = "회원을 ID 기반으로 삭제합니다.")
    @DeleteMapping("/me")
    fun deleteMember(): ResponseEntity<ApiResponse<Unit>> {
        // SecurityContext에서 인증된 회원 ID 가져오기
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
            ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
        val memberId = authentication.principal as Long

        memberService.deleteMember(memberId)
        
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(Unit, "회원 탈퇴가 완료되었습니다."))
    }
    @Operation(summary = "프로필 셋업하기", description = "프로필 초기에 어떤식으로 설정할지 정하는 api 입니다.")
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


    @Operation(summary = "역할 정하기", description = "FAN인지 ARTIST인지 정하는 api 인데 profile/setup과 합쳐질 수 있습니다.")
    @PatchMapping("/me/role")
    fun updateRole(@Valid @RequestBody request: RoleUpdateRequest): ResponseEntity<ApiResponse<RoleUpdateResponse>> {
        // SecurityContext에서 인증된 회원 ID 가져오기
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
            ?: throw BusinessException(ErrorCode.INVALID_TOKEN)
        val memberId = authentication.principal as Long

        val updatedMember = memberService.updateRole(memberId, request.role)
        
        val memberIdValue = updatedMember.id ?: run {
            log.error("Critical: Member ID is null after role update. Email: ${updatedMember.email}")
            throw IllegalStateException("회원 ID가 없습니다.")
        }
        
        val response = RoleUpdateResponse(
            memberId = memberIdValue,
            role = updatedMember.role
        )
        
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(response, "역할이 업데이트되었습니다."))
    }
}

