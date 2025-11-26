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
                auth.requestMatchers("/api/members/signup", "/api/members/login", "/api/members/token/refresh").permitAll()
                // 인증이 필요한 엔드포인트
                auth.requestMatchers("/api/members/logout").authenticated()
                // TODO: OAuth2 엔드포인트 추가 시 permitAll() 설정
                // auth.requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                
                // 나머지 요청은 개발 단계에서는 허용, 운영 환경에서는 authenticated()로 변경
                auth.anyRequest().permitAll()
            }
            // TODO: OAuth2 설정 추가
            // .oauth2Login { ... }
            // .oauth2ResourceServer { it.jwt { ... } }
        
        return http.build()
    }
}

