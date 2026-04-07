package com.bandchu.api.global.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class HttpRequestLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    private val excludePaths = listOf("/actuator", "/swagger-ui", "/v3/api-docs")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val uri = request.requestURI
        if (excludePaths.any { uri.startsWith(it) }) {
            filterChain.doFilter(request, response)
            return
        }

        val traceId = UUID.randomUUID().toString().substring(0, 8)
        val startTime = System.currentTimeMillis()

        MDC.put("traceId", traceId)
        MDC.put("requestUri", uri)
        MDC.put("requestMethod", request.method)
        MDC.put("clientIp", request.getHeader("X-Real-IP") ?: request.remoteAddr)

        try {
            filterChain.doFilter(request, response)
        } finally {
            val duration = System.currentTimeMillis() - startTime
            log.info(
                "{} {} {} {}ms",
                request.method,
                uri,
                response.status,
                duration
            )
            MDC.clear()
        }
    }
}
