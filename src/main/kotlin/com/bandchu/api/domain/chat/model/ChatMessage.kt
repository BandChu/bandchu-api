package com.bandchu.api.domain.chat.model

import kotlinx.datetime.LocalDateTime

data class ChatMessage (
    val chatmessage_id: Int? = null,
    val content: String? = null,
    val created_at: LocalDateTime? = null,
    val file_url: String? = null,
    val message_type: String? = null, // TEXT, FILE, IMAGE -> 이부분 enum으로 바꾸는게 좋을 지도?
    )