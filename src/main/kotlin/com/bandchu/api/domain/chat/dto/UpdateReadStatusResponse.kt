package com.bandchu.api.domain.chat.dto

/**
 * 읽음 처리 응답 DTO
 */
data class UpdateReadStatusResponse(
    val roomId: Long,
    val lastReadMessageId: Long
)
