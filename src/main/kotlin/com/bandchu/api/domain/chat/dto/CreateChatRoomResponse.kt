package com.bandchu.api.domain.chat.dto

import com.bandchu.api.domain.chat.table.RoomType
import java.time.OffsetDateTime

/**
 * 채팅방 생성 응답 DTO
 */
data class CreateChatRoomResponse(
    val roomId: Long,
    val roomType: RoomType,
    val name: String?,
    val createdAt: OffsetDateTime
)
