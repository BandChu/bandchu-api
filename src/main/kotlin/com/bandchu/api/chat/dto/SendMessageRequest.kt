package com.bandchu.api.chat.dto

import com.bandchu.api.chat.domain.MessageType

data class SendMessageRequest(
    val roomId: Long,
    val senderId: Long,
    val messageType: MessageType,
    val content: String?,
    val fileUrl: String?
)
