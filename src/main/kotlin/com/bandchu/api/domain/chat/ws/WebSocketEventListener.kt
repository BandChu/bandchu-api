package com.bandchu.api.domain.chat.ws

import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class WebSocketEventListener(
    private val sessionManager: WebSocketSessionManager
) {

     //클라이언트가 CONNECT 했을 때 호출됨
    @EventListener
    fun handleSessionConnected(event: SessionConnectEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)

        val sessionId = accessor.sessionId!!
        val userId = accessor.getFirstNativeHeader("X-User-Id")?.toLong()

        if (userId != null) {
            sessionManager.addSession(userId, sessionId)
        }
    }

     //클라이언트가 DISCONNECT 했을 때 호출됨
    @EventListener
    fun handleSessionDisconnected(event: SessionDisconnectEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val sessionId = accessor.sessionId!!
        sessionManager.removeSession(sessionId)
    }
}
