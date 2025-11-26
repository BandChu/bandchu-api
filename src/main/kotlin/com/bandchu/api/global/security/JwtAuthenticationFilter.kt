package com.bandchu.api.global.security

import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.net.URI
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)

            try {
                if (jwtService.validateToken(token)) {
                    val memberId = jwtService.getMemberIdFromToken(token)
                    val role = jwtService.getRoleFromToken(token)
                    val tokenType = jwtService.getTokenTypeFromToken(token)

                    // Access token만 허용
                    if (tokenType != "access") {
                        handleException(response, ErrorCode.INVALID_TOKEN, request.requestURI)
                        return
                    }

                    val authorities = listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
                    val authentication = UsernamePasswordAuthenticationToken(
                        memberId,
                        null,
                        authorities
                    ).apply {
                        details = WebAuthenticationDetailsSource().buildDetails(request)
                    }

                    SecurityContextHolder.getContext().authentication = authentication
                } else {
                    handleException(response, ErrorCode.INVALID_TOKEN, request.requestURI)
                    return
                }
            } catch (e: BusinessException) {
                handleException(response, e.errorCode, request.requestURI)
                return
            } catch (e: Exception) {
                handleException(response, ErrorCode.INVALID_TOKEN, request.requestURI)
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun handleException(
        response: HttpServletResponse,
        errorCode: ErrorCode,
        path: String
    ) {
        try {
            val detail = ProblemDetail.forStatusAndDetail(errorCode.httpStatus, errorCode.message).apply {
                type = URI.create("https://api.bandchu.com/errors/${errorCode.docUri}")
                title = errorCode.httpStatus.reasonPhrase
                setProperty("code", errorCode.code)
                setProperty("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString())
                setProperty("instance", path)
            }

            response.status = errorCode.httpStatus.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = "UTF-8"
            objectMapper.writeValue(response.outputStream, detail)
            response.outputStream.flush()
        } catch (e: Exception) {
            // 이미 응답이 커밋된 경우 무시
        }
    }
}

