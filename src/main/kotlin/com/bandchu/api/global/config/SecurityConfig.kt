package com.bandchu.api.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }                     // CSRF 비활성화
            .authorizeHttpRequests { it.anyRequest().permitAll() } // 모든 요청 허용
            .httpBasic { it.disable() }                // 기본 인증 비활성화
            .formLogin { it.disable() }                // 폼 로그인 비활성화

        return http.build()
    }
}
