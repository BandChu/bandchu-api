package com.bandchu.api.domain.chat.model

import java.time.OffsetDateTime

/**
 * 회원-채팅방 연결 엔티티
 * - 사용자가 참여한 채팅방과 읽음 상태를 관리
 */
data class MemberChatRoom(
    val id: Long,
    val roomId: Long,
    val memberId: Long,
    val role: String,                       // 추후 확장 가능 (admin, member 등)
    val lastReadMessageId: Long?,           // 마지막으로 읽은 메시지 ID (null이면 아직 안 읽음)
    val joinedAt: OffsetDateTime
)
