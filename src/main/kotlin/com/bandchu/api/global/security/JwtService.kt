package com.bandchu.api.global.security

import com.bandchu.api.domain.member.model.Role
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${jwt.secret:bandchu-secret-key-for-jwt-token-generation-minimum-256-bits}")
    private val secret: String,
    @Value("\${jwt.access-token-expiration:3600000}") // 1시간
    private val accessTokenExpiration: Long,
    @Value("\${jwt.refresh-token-expiration:604800000}") // 7일
    private val refreshTokenExpiration: Long
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateAccessToken(memberId: Long, role: Role): String {
        val now = Date()
        val expiryDate = Date(now.time + accessTokenExpiration)

        return Jwts.builder()
            .subject(memberId.toString())
            .claim("role", role.name)
            .claim("tokenType", "access")
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    fun generateRefreshToken(memberId: Long, role: Role): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshTokenExpiration)

        return Jwts.builder()
            .subject(memberId.toString())
            .claim("role", role.name)
            .claim("tokenType", "refresh")
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getMemberIdFromToken(token: String): Long {
        val claims = getClaims(token)
        return claims.subject.toLong()
    }

    fun getRoleFromToken(token: String): Role {
        val claims = getClaims(token)
        val roleString = claims["role"] as? String
            ?: throw IllegalArgumentException("Token does not contain role claim")
        return Role.valueOf(roleString)
    }

    fun getTokenTypeFromToken(token: String): String {
        val claims = getClaims(token)
        return claims["tokenType"] as? String
            ?: throw IllegalArgumentException("Token does not contain tokenType claim")
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}

