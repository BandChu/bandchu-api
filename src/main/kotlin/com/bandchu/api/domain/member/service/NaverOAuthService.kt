package com.bandchu.api.domain.member.service

import com.bandchu.api.domain.member.dto.NaverUserInfo
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.slf4j.LoggerFactory  // 이걸로 바꿔야 합니다
import org.springframework.beans.factory.annotation.Value

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class NaverOAuthService(
    @Value("\${oauth.naver.client-id}") private val clientId: String,
    @Value("\${oauth.naver.client-secret}") private val clientSecret: String,
    private val restTemplate: RestTemplate
) {
    private val naverApiUri = "https://openapi.naver.com/v1/nid/me"
    private val log = LoggerFactory.getLogger(javaClass)


    /**
     * 1단계: 인증 코드(code)를 Access Token으로 교환
     */
    fun getAccessToken(code: String, state: String): String {
        val uri = UriComponentsBuilder
            .fromHttpUrl("https://nid.naver.com/oauth2.0/token")
            .queryParam("grant_type", "authorization_code")
            .queryParam("client_id", clientId)
            .queryParam("client_secret", clientSecret)
            .queryParam("code", code)
            .queryParam("state", state)
            .build()
            .toUri()

        val response = restTemplate.getForObject(uri, Map::class.java)
            ?: throw BusinessException(ErrorCode.NAVER_AUTH_INVALID)

        return response["access_token"] as? String
            ?: throw BusinessException(ErrorCode.NAVER_AUTH_INVALID)
    }

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