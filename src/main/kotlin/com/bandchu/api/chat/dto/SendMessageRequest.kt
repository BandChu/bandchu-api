package com.bandchu.api.chat.dto

import com.bandchu.api.chat.persistence.table.MessageType

data class SendMessageRequest(
    val messageType: MessageType,
    val content: String?,
    val fileUrl: String?
)
