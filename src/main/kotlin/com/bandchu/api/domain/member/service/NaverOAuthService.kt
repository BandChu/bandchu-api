package com.bandchu.api.domain.member.service

import com.bandchu.api.domain.member.dto.NaverUserInfo
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.slf4j.LoggerFactory  // 이걸로 바꿔야 합니다

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class NaverOAuthService(
    private val restTemplate: RestTemplate
) {
    private val naverApiUri = "https://openapi.naver.com/v1/nid/me"
    private val log = LoggerFactory.getLogger(javaClass)
    fun verifyToken(accessToken: String): NaverUserInfo {
        val headers = HttpHeaders().apply { setBearerAuth(accessToken) }

        try {
            val responseEntity = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                HttpEntity<Unit>(headers),
                Map::class.java
            )

            val body = responseEntity.body ?: throw BusinessException(ErrorCode.NAVER_AUTH_INVALID)

            // 핵심: 'response' 키 안에 실제 데이터가 들어있음
            @Suppress("UNCHECKED_CAST")
            val naverResponse = body["response"] as? Map<String, Any>
                ?: throw BusinessException(ErrorCode.NAVER_AUTH_INVALID)

            return NaverUserInfo(
                naverId = naverResponse["id"] as String,
                email = naverResponse["email"] as String,
                nickname = naverResponse["name"] as? String,
                profileImage = naverResponse["profile_image"] as? String
            )
        } catch (e: Exception) {
            log.error("Naver Auth Failed: {}", e.message, e)
            throw BusinessException(ErrorCode.NAVER_AUTH_INVALID)
        }
    }
}