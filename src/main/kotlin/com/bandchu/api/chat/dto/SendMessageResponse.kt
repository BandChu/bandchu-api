package com.bandchu.api.chat.dto

import com.bandchu.api.chat.domain.ChatMessage
import com.bandchu.api.chat.domain.MessageType
import java.time.LocalDateTime

data class ChatMessageResponse(
    val messageId: Long,
    val roomId: Long,
    val senderId: Long,
    val messageType: MessageType,
    val content: String?,
    val fileUrl: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(message: ChatMessage){
            ChatMessageResponse(
                messageId = message.id,
                roomId = message.roomId,
                senderId = message.senderId,
                messageType = message.messageType,
                content = message.content,
                fileUrl = message.fileUrl,
                createdAt = message.createdAt
            )
        }
    }
}
