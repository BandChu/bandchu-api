package com.bandchu.api.domain.member.service

import com.bandchu.api.domain.member.dto.LoginRequest
import com.bandchu.api.domain.member.dto.SignupRequest
import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.domain.member.repository.MemberRepository
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import com.bandchu.api.global.security.JwtService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository,
    private val jwtService: JwtService,
    private val googleOAuthService: GoogleOAuthService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun signup(request: SignupRequest): Member {
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(request.email)) {
            throw BusinessException(ErrorCode.USER_EMAIL_DUPLICATED)
        }

        // 회원 생성 (일반 회원가입은 프로필 정보를 이미 받으므로 완료 상태로 설정)
        val member = Member(
            email = request.email,
            password = request.password, // TODO: 비밀번호 암호화 추가 필요
            nickname = request.nickname,
            role = request.role,
            isProfileCompleted = true
        )

        return memberRepository.save(member)
    }

    fun login(request: LoginRequest): LoginResult {
        // 이메일로 회원 조회
        val member = memberRepository.findByEmail(request.email)
            ?: throw BusinessException(ErrorCode.USER_INVALID_CREDENTIAL)

        // 비밀번호 검증
        // TODO: 비밀번호 암호화 추가 후 BCrypt 등으로 비교
        if (member.password != request.password) {
            throw BusinessException(ErrorCode.USER_INVALID_CREDENTIAL)
        }

        val memberId = member.id ?: run {
            log.error("Critical: Member ID is null after login. Email: ${member.email}")
            throw IllegalStateException("회원 ID가 없습니다.")
        }

        // JWT 토큰 생성
        val accessToken = jwtService.generateAccessToken(memberId, member.role)
        val refreshToken = jwtService.generateRefreshToken(memberId, member.role)

        return LoginResult(
            accessToken = accessToken,
            refreshToken = refreshToken,
            memberId = memberId,
            nickname = member.nickname
        )
    }

    fun logout() {
        // JWT는 stateless이므로 토큰 검증은 필터에서 처리
        // 여기서는 추가적인 로그아웃 로직이 필요하면 구현
        // 예: 토큰 블랙리스트 관리, refresh token 무효화 등
    }

    fun refreshToken(refreshToken: String): TokenPair {
        // 리프레시 토큰 검증
        if (!jwtService.validateToken(refreshToken)) {
            throw BusinessException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        // 토큰 타입 확인 (refresh token만 허용)
        val tokenType = try {
            jwtService.getTokenTypeFromToken(refreshToken)
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        if (tokenType != "refresh") {
            throw BusinessException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        // 회원 ID와 역할 추출
        val memberId = try {
            jwtService.getMemberIdFromToken(refreshToken)
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        val role = try {
            jwtService.getRoleFromToken(refreshToken)
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        // 회원 존재 확인
        memberRepository.findById(memberId)
            ?: throw BusinessException(ErrorCode.INVALID_REFRESH_TOKEN)

        // 새로운 토큰 발급
        val newAccessToken = jwtService.generateAccessToken(memberId, role)
        val newRefreshToken = jwtService.generateRefreshToken(memberId, role)

        return TokenPair(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    fun googleLogin(idToken: String): GoogleOAuthResult {
        // Google ID Token 검증
        val googleUserInfo = try {
            googleOAuthService.verifyIdToken(idToken)
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.GOOGLE_AUTH_INVALID)
        }

        // 이메일로 기존 회원 조회
        val existingMember = memberRepository.findByEmail(googleUserInfo.email)
        val isNewMember = existingMember == null
        
        val member = if (existingMember != null) {
            // 기존 회원인 경우
            // Google ID가 아직 연결되지 않은 경우 자동으로 연결
            if (existingMember.googleId == null) {
                val memberId = existingMember.id ?: run {
                    log.error("Critical: Member ID is null when updating Google ID. Email: ${existingMember.email}")
                    throw IllegalStateException("회원 ID가 없습니다.")
                }
                memberRepository.updateGoogleId(memberId, googleUserInfo.googleId)
                existingMember.copy(googleId = googleUserInfo.googleId)
            } else {
                existingMember
            }
        } else {
            // 신규 회원인 경우 자동 가입 (프로필 미완료 상태로 생성)
            val newMember = Member(
                email = googleUserInfo.email,
                password = "", // OAuth 회원은 비밀번호 없음
                nickname = googleUserInfo.name,
                role = Role.FAN, // 기본 역할은 FAN
                googleId = googleUserInfo.googleId,
                isProfileCompleted = false // 구글 OAuth 회원가입은 프로필 설정 전까지 미완료 상태
            )
            memberRepository.save(newMember)
        }

        val memberId = member.id ?: run {
            log.error("Critical: Member ID is null after login. Email: ${member.email}")
            throw IllegalStateException("회원 ID가 없습니다.")
        }

        // JWT 토큰 생성
        val accessToken = jwtService.generateAccessToken(memberId, member.role)
        val refreshToken = jwtService.generateRefreshToken(memberId, member.role)

        return GoogleOAuthResult(
            accessToken = accessToken,
            refreshToken = refreshToken,
            isNewMember = isNewMember,
            memberId = memberId,
            nickname = member.nickname
        )
    }

    fun verifyOAuth(provider: String, token: String): OAuthVerifyResult {
        // 프로바이더별 토큰 검증 및 사용자 정보 추출
        val userInfo = when (provider.uppercase()) {
            "GOOGLE" -> {
                try {
                    googleOAuthService.verifyIdToken(token)
                } catch (e: BusinessException) {
                    throw BusinessException(ErrorCode.OAUTH_TOKEN_INVALID)
                } catch (e: Exception) {
                    throw BusinessException(ErrorCode.OAUTH_TOKEN_INVALID)
                }
            }
            else -> throw BusinessException(ErrorCode.OAUTH_TOKEN_INVALID)
        }

        // 이메일로 기존 회원 조회
        val member = memberRepository.findByEmail(userInfo.email)
            ?: throw BusinessException(ErrorCode.OAUTH_TOKEN_INVALID)

        val memberId = member.id ?: run {
            log.error("Critical: Member ID is null after login. Email: ${member.email}")
            throw IllegalStateException("회원 ID가 없습니다.")
        }

        // JWT 토큰 생성
        val accessToken = jwtService.generateAccessToken(memberId, member.role)
        val refreshToken = jwtService.generateRefreshToken(memberId, member.role)

        return OAuthVerifyResult(
            accessToken = accessToken,
            refreshToken = refreshToken,
            memberId = memberId
        )
    }

    fun linkOAuth(memberId: Long, provider: String, token: String): OAuthLinkResult {
        // 프로바이더별 토큰 검증 및 사용자 정보 추출
        val userInfo = when (provider.uppercase()) {
            "GOOGLE" -> {
                try {
                    googleOAuthService.verifyIdToken(token)
                } catch (e: BusinessException) {
                    throw BusinessException(ErrorCode.OAUTH_TOKEN_INVALID)
                } catch (e: Exception) {
                    throw BusinessException(ErrorCode.OAUTH_TOKEN_INVALID)
                }
            }
            else -> throw BusinessException(ErrorCode.OAUTH_TOKEN_INVALID)
        }

        // 현재 회원 조회
        memberRepository.findById(memberId)
            ?: run {
                log.error("Critical: Member not found by ID. MemberId: $memberId")
                throw IllegalStateException("회원을 찾을 수 없습니다.")
            }

        // 이미 연결된 계정인지 확인 (다른 회원이 이미 이 Google ID를 사용 중인 경우)
        val existingMemberWithGoogleId = memberRepository.findByGoogleId(userInfo.googleId)
        if (existingMemberWithGoogleId != null && existingMemberWithGoogleId.id != memberId) {
            throw BusinessException(ErrorCode.OAUTH_ALREADY_LINKED)
        }

        // Google ID 연결
        memberRepository.updateGoogleId(memberId, userInfo.googleId)

        return OAuthLinkResult(
            linkedProvider = provider.uppercase()
        )
    }

    fun deleteMember(memberId: Long) {
        // 회원 존재 확인
        val member = memberRepository.findById(memberId)
        
        // 회원이 이미 삭제된 경우에도 성공으로 처리 (DELETE는 idempotent)
        if (member == null) {
            log.info("Member already deleted. MemberId: $memberId")
            return
        }

        // 회원 삭제
        memberRepository.deleteById(memberId)
    }

    fun setupProfile(memberId: Long, nickname: String, profileImageUrl: String?): Member {
        // 회원 존재 확인
        memberRepository.findById(memberId)
            ?: run {
                log.error("Critical: Member not found by ID. MemberId: $memberId")
                throw IllegalStateException("회원을 찾을 수 없습니다.")
            }

        // 닉네임 유효성 검증은 DTO의 @Pattern 어노테이션에 의해 처리됩니다.

        // 프로필 업데이트
        return memberRepository.updateProfile(memberId, nickname, profileImageUrl)
    }

    fun updateRole(memberId: Long, role: Role): Member {
        // 회원 존재 확인
        memberRepository.findById(memberId)
            ?: run {
                log.error("Critical: Member not found by ID. MemberId: $memberId")
                throw IllegalStateException("회원을 찾을 수 없습니다.")
            }

        // 역할 업데이트
        return memberRepository.updateRole(memberId, role)
    }
}

