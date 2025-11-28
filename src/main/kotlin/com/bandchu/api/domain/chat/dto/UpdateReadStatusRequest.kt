package com.bandchu.api.domain.chat.dto

/**
 * 읽음 처리 요청 DTO
 * - lastReadMessageId: 마지막으로 읽은 메시지 ID
 */
data class UpdateReadStatusRequest(
    val lastReadMessageId: Long
)
