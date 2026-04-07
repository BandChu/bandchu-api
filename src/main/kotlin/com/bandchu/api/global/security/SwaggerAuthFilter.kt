package com.bandchu.api.global.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.Base64

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class SwaggerAuthFilter(
    @Value("\${swagger.auth.username}") private val username: String,
    @Value("\${swagger.auth.password}") private val password: String
) : OncePerRequestFilter() {

    private val swaggerPaths = listOf("/swagger-ui", "/v3/api-docs", "/swagger-resources")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.requestURI

        if (swaggerPaths.none { path.startsWith(it) }) {
            filterChain.doFilter(request, response)
            return
        }

        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            val decoded = String(Base64.getDecoder().decode(authHeader.substring(6)))
            val parts = decoded.split(":", limit = 2)
            if (parts.size == 2 && parts[0] == username && parts[1] == password) {
                filterChain.doFilter(request, response)
                return
            }
        }

        response.setHeader("WWW-Authenticate", "Basic realm=\"Swagger UI\"")
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
    }
}
