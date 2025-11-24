package com.bandchu.api.chat.domain

import java.time.LocalDateTime

data class ChatMessage(
    val id: Long,
    val roomId: Long,
    val senderId: Long,
    val messageType: MessageType,
    val content: String?,
    val fileUrl: String?,
    val createdAt: LocalDateTime = LocalDateTime.now()
)