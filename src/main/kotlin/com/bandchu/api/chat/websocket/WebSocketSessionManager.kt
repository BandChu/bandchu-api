package com.bandchu.api.chat.websocket

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class WebSocketSessionManager {
    // userId → Set<sessionId>
    private val userSessions: MutableMap<Long, MutableSet<String>> = ConcurrentHashMap()

    // sessionId → userId
    private val sessionUserMap: MutableMap<String, Long> = ConcurrentHashMap()

    fun addSession(userId: Long, sessionId: String) {
        userSessions.computeIfAbsent(userId) { ConcurrentHashMap.newKeySet() }.add(sessionId)
        sessionUserMap[sessionId] = userId
        println("[WS] session added: user=$userId, session=$sessionId")
    }

    fun removeSession(sessionId: String) {
        val userId = sessionUserMap[sessionId] ?: return
        userSessions[userId]?.remove(sessionId)

        if (userSessions[userId]?.isEmpty() == true) {
            userSessions.remove(userId)
        }

        sessionUserMap.remove(sessionId)
        println("[WS] session removed: user=$userId, session=$sessionId")
    }

    fun getSessionsByUserId(userId: Long): Set<String> {
        return userSessions[userId] ?: emptySet()
    }

    fun getUserIdBySession(sessionId: String): Long? {
        return sessionUserMap[sessionId]
    }
}
