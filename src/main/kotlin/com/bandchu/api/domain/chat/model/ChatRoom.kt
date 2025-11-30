package com.bandchu.api.domain.chat.model

import com.bandchu.api.domain.chat.table.RoomType
import java.time.OffsetDateTime

/**
 * 채팅방 엔티티
 * - 1:1 또는 그룹 채팅방 정보를 담는 도메인 모델
 */
data class ChatRoom(
    val id: Long,
    val name: String?,           // DIRECT는 null 가능
    val roomType: RoomType,      // DIRECT or GROUP
    val createdAt: OffsetDateTime
)
