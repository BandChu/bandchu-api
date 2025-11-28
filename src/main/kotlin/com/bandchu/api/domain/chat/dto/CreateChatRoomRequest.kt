package com.bandchu.api.domain.chat.dto

import com.bandchu.api.domain.chat.table.RoomType

/**
 * 채팅방 생성 요청 DTO
 * - roomType: DIRECT(1:1) 또는 GROUP(그룹)
 * - name: 채팅방 이름 (DIRECT는 null 가능)
 * - memberIds: 초대할 회원 ID 목록 (본인 제외)
 */
data class CreateChatRoomRequest(
    val roomType: RoomType,
    val name: String?,
    val memberIds: List<Long>
)
