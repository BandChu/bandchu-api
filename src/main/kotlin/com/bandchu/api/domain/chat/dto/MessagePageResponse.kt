package com.bandchu.api.domain.chat.dto

data class MessagePageResponse(
    val messages: List<ChatMessageResponse>,
    val nextCursor: Long?
)
