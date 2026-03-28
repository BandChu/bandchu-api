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

    private val kauthUri = "https://kauth.kakao.com/oauth/token"


    fun getAccessToken(code: String): String {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        // 카카오는 Map 대신 MultiValueMap을 선호합니다 (RestTemplate 규격)
        val params = org.springframework.util.LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", "0129cc6caf5e4310e4e4510d9b8cb9d4") // REST API 키
            add("redirect_uri", "http://localhost:8080/api/members/oauth/kakao/callback")
            add("code", code)
            // 만약 Client Secret을 ON 하셨다면 아래 줄 주석 해제해서 추가하세요!
            // add("client_secret", "H92J4i7mjVVznaXLMTKl...")
        }

        return try {
            val response = restTemplate.postForObject(
                kauthUri,
                HttpEntity(params, headers),
                Map::class.java
            ) ?: throw BusinessException(ErrorCode.KAKAO_AUTH_INVALID)

            response["access_token"]?.toString()
                ?: throw BusinessException(ErrorCode.KAKAO_AUTH_INVALID)
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.KAKAO_AUTH_INVALID)
        }
    }

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