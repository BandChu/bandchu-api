package com.bandchu.api.domain.member.service

import com.bandchu.api.domain.member.dto.KakaoUserInfo
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.function.RequestPredicates.contentType

@Service

class KakaoOAuthService(
    private val restTemplate: RestTemplate
) {
    private val kapiUri = "https://kapi.kakao.com/v2/user/me"

    fun verifyToken(accessToken: String): KakaoUserInfo {
        val headers = HttpHeaders().apply {
            setBearerAuth(accessToken)
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        return try {
            val response = restTemplate.postForObject(kapiUri, HttpEntity<Unit>(headers), Map::class.java)
                ?: throw BusinessException(ErrorCode.KAKAO_AUTH_INVALID)

            // 1. 카카오 고유 ID
            val id = response["id"]?.toString() ?: throw BusinessException(ErrorCode.KAKAO_AUTH_INVALID)

            // 2. 계정 정보(email) 추출
            val kakaoAccount = response["kakao_account"] as? Map<*, *>
            val email = kakaoAccount?.get("email") as? String ?: throw BusinessException(ErrorCode.KAKAO_AUTH_INVALID)

            // 3. 프로필 정보(nickname) 추출 - 여기가 포인트!
            val profile = kakaoAccount["profile"] as? Map<*, *>
            val nickname = profile?.get("nickname") as? String

            KakaoUserInfo(
                kakaoId = id,
                email = email,
                nickname = nickname // 닉네임 전달
            )
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.KAKAO_AUTH_INVALID)
        }
    }
}