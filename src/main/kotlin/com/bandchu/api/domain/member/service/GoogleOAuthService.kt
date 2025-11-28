package com.bandchu.api.domain.member.service

import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.Base64
import java.util.Date

/**
 * Google OAuth ID Token 검증 서비스
 * 
 * Google ID Token을 검증하고 사용자 정보를 추출합니다.
 * - JWT 파싱 및 구조 검증
 * - 클레임 검증 (iss, aud, exp)
 * - 서명 검증 (Google 공개키 사용)
 */
@Service
class GoogleOAuthService(
    @Value("\${oauth.google.client-id}")
    private val googleClientId: String,
    @Value("\${spring.security.oauth2.client.provider.google.jwk-set-uri}")
    private val jwkSetUri: String,
    private val objectMapper: ObjectMapper,
    private val restTemplate: RestTemplate
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Google ID Token을 검증하고 사용자 정보를 추출합니다.
     * 
     * @param idToken Google ID Token
     * @return GoogleUserInfo (email, name 등)
     * @throws BusinessException 토큰이 유효하지 않은 경우
     */
    fun verifyIdToken(idToken: String): GoogleUserInfo {
        val parts = parseJwtParts(idToken)
        val claims = verifySignatureAndParseClaims(parts)
        validateClaims(claims)
        return extractUserInfo(claims)
    }
    
    /**
     * JWT 토큰을 파싱하여 헤더, 페이로드, 서명 부분을 분리합니다.
     */
    private fun parseJwtParts(idToken: String): List<String> {
        if (idToken.isBlank()) {
            throw BusinessException(ErrorCode.GOOGLE_AUTH_INVALID)
        }
        
        val parts = idToken.split(".")
        if (parts.size != 3) {
            throw BusinessException(ErrorCode.GOOGLE_AUTH_INVALID)
        }
        
        return parts
    }
    
    /**
     * JWT 서명을 검증하고 Claims를 추출합니다.
     */
    private fun verifySignatureAndParseClaims(parts: List<String>): Claims {
        try {
            val header = parts[0]
            val decodedHeader = Base64.getUrlDecoder().decode(header)
            val headerJson = String(decodedHeader)
            @Suppress("UNCHECKED_CAST")
            val headerMap = objectMapper.readValue(headerJson, Map::class.java) as Map<String, Any>
            val kid = headerMap["kid"] as? String ?: throw BusinessException(ErrorCode.GOOGLE_AUTH_INVALID)
            
            val publicKey = getPublicKeyFromJwkSet(kid)
            val fullToken = "${parts[0]}.${parts[1]}.${parts[2]}"
            
            return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(fullToken)
                .payload
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            log.error("Failed to verify JWT signature and parse claims", e)
            throw BusinessException(ErrorCode.GOOGLE_AUTH_INVALID)
        }
    }
    
    /**
     * Google JWK Set에서 kid에 해당하는 공개키를 가져옵니다.
     */
    private fun getPublicKeyFromJwkSet(kid: String): PublicKey {
        try {
            val response = restTemplate.getForObject(jwkSetUri, Map::class.java)
            @Suppress("UNCHECKED_CAST")
            val keys = (response?.get("keys") as? List<Map<String, Any>>)
                ?: throw BusinessException(ErrorCode.GOOGLE_AUTH_INVALID)
            
            val jwk = keys.firstOrNull { it["kid"] == kid }
                ?: throw BusinessException(ErrorCode.GOOGLE_AUTH_INVALID)
            
            val modulusStr = jwk["n"] as String
            val exponentStr = jwk["e"] as String
            
            val modulusBytes = Base64.getUrlDecoder().decode(modulusStr)
            val exponentBytes = Base64.getUrlDecoder().decode(exponentStr)
            
            val modulusBigInt = BigInteger(1, modulusBytes)
            val exponentBigInt = BigInteger(1, exponentBytes)
            
            val keySpec = RSAPublicKeySpec(modulusBigInt, exponentBigInt)
            val keyFactory = KeyFactory.getInstance("RSA")
            
            return keyFactory.generatePublic(keySpec)
        } catch (e: BusinessException) {
            throw e
        } catch (e: Exception) {
            log.error("Failed to get public key from JWK Set (kid: $kid, uri: $jwkSetUri)", e)
            throw BusinessException(ErrorCode.GOOGLE_AUTH_INVALID)
        }
    }
    
    /**
     * 클레임을 검증합니다.
     * - iss: Google 발급자 확인
     * - aud: 클라이언트 ID 확인
     * - exp: 만료 시간 확인
     */
    private fun validateClaims(claims: Claims) {
        val issuer = claims["iss"] as? String
        if (issuer == null || !issuer.contains("accounts.google.com")) {
            throw BusinessException(ErrorCode.GOOGLE_AUTH_INVALID)
        }
        
        val audienceObj = claims["aud"]
        val audience = when (audienceObj) {
            is String -> audienceObj
            is Collection<*> -> audienceObj.firstOrNull() as? String
            else -> null
        }
        
        if (audience == null || audience != googleClientId) {
            throw BusinessException(ErrorCode.GOOGLE_AUTH_INVALID)
        }
        
        val expiration = claims.expiration
        if (expiration == null || expiration.before(Date())) {
            throw BusinessException(ErrorCode.GOOGLE_AUTH_INVALID)
        }
    }
    
    /**
     * 클레임에서 사용자 정보를 추출합니다.
     */
    private fun extractUserInfo(claims: Claims): GoogleUserInfo {
        val googleId = claims.subject ?: throw BusinessException(ErrorCode.GOOGLE_AUTH_INVALID)
        val email = claims["email"] as? String ?: throw BusinessException(ErrorCode.GOOGLE_AUTH_INVALID)
        val name = claims["name"] as? String ?: ""
        val picture = claims["picture"] as? String
        
        return GoogleUserInfo(
            googleId = googleId,
            email = email,
            name = name,
            picture = picture
        )
    }
}

/**
 * Google 사용자 정보
 */
data class GoogleUserInfo(
    val googleId: String,
    val email: String,
    val name: String,
    val picture: String?
)

