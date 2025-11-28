package com.bandchu.api.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

/**
 * Spring Security 설정
 * TODO: JWT 인증 구현 후 제거
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/chatrooms/**").permitAll()  // 채팅 API 임시 오픈
                    .requestMatchers("/ws-chat/**").permitAll()  // WS API 임시 오픈
                    .anyRequest().authenticated()
            }
        
        return http.build()
    }
}
