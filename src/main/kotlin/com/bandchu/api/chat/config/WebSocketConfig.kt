package com.bandchu.api.chat.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws-chat") // 클라이언트 접속 엔드포인트
            .setAllowedOriginPatterns("*") // 모든 도메인 허용
            .withSockJS() // SockJS fallback 지원
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic") // 메시지 브로커 prefix
        registry.setApplicationDestinationPrefixes("/app") // 메시지 매핑 prefix
    }
}