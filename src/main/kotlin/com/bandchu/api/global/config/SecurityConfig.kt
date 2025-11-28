package com.bandchu.api.global.config

import com.bandchu.api.global.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT 사용을 위한 STATELESS
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests { auth ->
                // 공개 엔드포인트
                auth.requestMatchers(
                    "/api/members/signup",
                    "/api/members/login",
                    "/api/members/token/refresh",
                    "/api/members/oauth/google",
                    "/api/members/oauth/verify"
                ).permitAll()
                // 인증이 필요한 엔드포인트
                auth.requestMatchers("/api/members/logout", "/api/members/me/**").authenticated()
                
                // 나머지 요청은 개발 단계에서는 허용, 운영 환경에서는 authenticated()로 변경
                auth.anyRequest().permitAll()
            }
        
        return http.build()
    }
}

