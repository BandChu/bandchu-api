package com.bandchu.api.domain.member.service

import com.bandchu.api.domain.member.dto.LoginRequest
import com.bandchu.api.domain.member.dto.LoginResponse
import com.bandchu.api.domain.member.dto.RefreshTokenRequest
import com.bandchu.api.domain.member.dto.RefreshTokenResponse
import com.bandchu.api.domain.member.dto.SignupRequest
import com.bandchu.api.domain.member.dto.SignupResponse
import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.repository.MemberRepository
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import com.bandchu.api.global.security.JwtService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository,
    private val jwtService: JwtService
) {

    fun signup(request: SignupRequest): SignupResponse {
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(request.email)) {
            throw BusinessException(ErrorCode.USER_EMAIL_DUPLICATED)
        }

        // 회원 생성
        val member = Member(
            email = request.email,
            password = request.password, // TODO: 비밀번호 암호화 추가 필요
            nickname = request.nickname,
            role = request.role
        )

        val savedMember = memberRepository.save(member)

        return SignupResponse(
            memberId = savedMember.id ?: throw IllegalStateException("회원 저장 후 ID가 없습니다."),
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
    }

    fun login(request: LoginRequest): LoginResponse {
        // 이메일로 회원 조회
        val member = memberRepository.findByEmail(request.email)
            ?: throw BusinessException(ErrorCode.USER_INVALID_CREDENTIAL)

        // 비밀번호 검증
        // TODO: 비밀번호 암호화 추가 후 BCrypt 등으로 비교
        if (member.password != request.password) {
            throw BusinessException(ErrorCode.USER_INVALID_CREDENTIAL)
        }

        val memberId = member.id ?: throw IllegalStateException("회원 ID가 없습니다.")

        // JWT 토큰 생성
        val accessToken = jwtService.generateAccessToken(memberId, member.role)
        val refreshToken = jwtService.generateRefreshToken(memberId, member.role)

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            memberId = memberId,
            nickname = member.nickname
        )
    }

    fun logout(token: String) {
        // JWT는 stateless이므로 토큰 검증은 필터에서 처리
        // 여기서는 추가적인 로그아웃 로직이 필요하면 구현
        // 예: 토큰 블랙리스트 관리, refresh token 무효화 등
    }

    fun refreshToken(refreshToken: String): RefreshTokenResponse {
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
        val member = memberRepository.findById(memberId)
            ?: throw BusinessException(ErrorCode.INVALID_REFRESH_TOKEN)

        // 새로운 토큰 발급
        val newAccessToken = jwtService.generateAccessToken(memberId, role)
        val newRefreshToken = jwtService.generateRefreshToken(memberId, role)

        return RefreshTokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }
}

