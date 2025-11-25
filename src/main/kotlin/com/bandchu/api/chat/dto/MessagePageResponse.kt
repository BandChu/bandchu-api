package com.bandchu.api.chat.dto

data class MessagePageResponse(
    val messages: List<ChatMessageResponse>,
    val nextCursor: Long?
)
