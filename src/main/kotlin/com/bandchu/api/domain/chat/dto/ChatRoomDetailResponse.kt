package com.bandchu.api.domain.chat.dto

import com.bandchu.api.domain.chat.table.RoomType
import java.time.OffsetDateTime

/**
 * 채팅방 상세 정보 응답 DTO
 */
data class ChatRoomDetailResponse(
    val roomId: Long,
    val roomType: RoomType,
    val name: String,
    val members: List<ChatRoomMemberDetail>,
    val createdAt: OffsetDateTime
)

/**
 * 채팅방 멤버 상세 정보
 */
data class ChatRoomMemberDetail(
    val userId: Long,
    val username: String,
    val profileImage: String? = null,
    val role: String? = null
)

