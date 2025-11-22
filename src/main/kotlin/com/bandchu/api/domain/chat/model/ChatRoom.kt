package com.bandchu.api.domain.chat.model

import kotlinx.datetime.LocalDateTime

data class ChatRoom (
    val room_id: Int? = null,
    val room_type: RoomType? = null,
    val created_at: LocalDateTime? = null,



    )