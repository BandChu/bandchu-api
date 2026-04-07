package com.bandchu.api.global.config

import com.bandchu.api.global.security.JwtAuthenticationFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    @Value("\${spring.security.bcrypt.strength:10}")
    private val bcryptStrength: Int
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        // BCrypt 워크 팩터 설정 (4-31, 기본값: 10)
        // 값이 높을수록 해시 계산 시간이 길어져 보안이 강화되지만, 응답 시간도 증가
        return BCryptPasswordEncoder(bcryptStrength)
    }

    @Bean // CORS 설정 - 채팅 및 프론트엔드와의 통신을 위해 추가했음. 
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("http://localhost:3000", "http://localhost:5173", "http://localhost:8000", "https://bandchu-front.vercel.app","https://band-chu.com")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.exposedHeaders = listOf("Authorization")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }


    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors {}
            .csrf { it.disable() }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests { auth ->

                // ===== Actuator (내부 전용) =====
                auth.requestMatchers("/actuator/**").permitAll()

                // ===== Swagger (Basic Auth 필터에서 별도 처리) =====
                auth.requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/swagger-resources/**"
                ).permitAll()

                // ===== 공개 엔드포인트 =====
                auth.requestMatchers(
                    "/api/members/signup",
                    "/api/members/login",
                    "/api/members/token/refresh",
                    "/api/members/oauth/google",
                    "/api/members/oauth/verify",
                    "/api/config/google-client-id"
                ).permitAll()

                // ===== chat & ws 엔드포인트 =====
                auth.requestMatchers("/api/chatrooms/**").permitAll()
                auth.requestMatchers("/ws-chat/**").permitAll()

                // ===== posts 엔드포인트 =====
                auth.requestMatchers("/api/posts/**").permitAll()

                // ===== 보호 엔드포인트 =====
                auth.requestMatchers(
                    "/api/members/logout",
                    "/api/members/me/**",
                    "/api/subscriptions/**"
                ).authenticated()

                // 개발 단계에서는 나머지 요청 허용 → 운영에서는 authenticated()로 변경 가능
                auth.anyRequest().permitAll()
            }

        return http.build()
    }
}
