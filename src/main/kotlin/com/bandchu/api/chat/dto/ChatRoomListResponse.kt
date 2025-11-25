package com.bandchu.api.chat.dto

import com.bandchu.api.domain.chat.table.RoomType
import java.time.OffsetDateTime

/**
 * 채팅방 목록 응답 DTO
 */
data class ChatRoomListResponse(
    val rooms: List<ChatRoomSummary>
)

/**
 * 채팅방 요약 정보
 * - unreadCount: 읽지 않은 메시지 개수
 * - lastMessage: 마지막 메시지 내용
 * - updatedAt: 마지막 메시지 시간
 */
data class ChatRoomSummary(
    val roomId: Long,
    val roomType: RoomType,
    val name: String?,
    val lastMessage: String?,
    val unreadCount: Int,
    val updatedAt: OffsetDateTime?
)
