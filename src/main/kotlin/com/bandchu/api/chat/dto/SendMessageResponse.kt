package com.bandchu.api.chat.dto

import com.bandchu.api.chat.domain.MessageType
import com.bandchu.api.chat.persistence.table.ChatMessages
import org.jetbrains.exposed.v1.core.ResultRow

data class ChatMessageResponse(
    val messageId: Long,
    val roomId: Long,
    val senderId: Long,
    val messageType: MessageType,
    val content: String?,
    val fileUrl: String?,
    val createdAt: kotlinx.datetime.LocalDateTime
) {
    companion object {
        fun from(row: ResultRow): ChatMessageResponse {
            return ChatMessageResponse(
                messageId = row[ChatMessages.id],
                roomId = row[ChatMessages.room],
                senderId = row[ChatMessages.sender],
                messageType = row[ChatMessages.messageType],
                content = row[ChatMessages.content],
                fileUrl = row[ChatMessages.fileUrl],
                createdAt = row[ChatMessages.createdAt]
            )
        }
    }
}
