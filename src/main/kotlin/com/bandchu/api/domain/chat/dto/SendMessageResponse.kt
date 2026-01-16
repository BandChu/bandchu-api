package com.bandchu.api.domain.chat.dto

import com.bandchu.api.domain.chat.table.MessageType
import com.bandchu.api.domain.chat.table.ChatMessageTable
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.OffsetDateTime

data class ChatMessageResponse(
    val messageId: Long,
    val roomId: Long,
    val senderId: Long,
    val messageType: MessageType,
    val content: String?,
    val fileUrl: String?,
    val createdAt: OffsetDateTime?
) {
    companion object {
        fun from(row: ResultRow): ChatMessageResponse {
            return ChatMessageResponse(
                messageId = row[ChatMessageTable.id],
                roomId = row[ChatMessageTable.roomId],
                senderId = row[ChatMessageTable.senderId].value,
                messageType = row[ChatMessageTable.messageType],
                content = row[ChatMessageTable.content],
                fileUrl = row[ChatMessageTable.fileUrl],
                createdAt = row[ChatMessageTable.createdAt]
            )
        }
    }
}
