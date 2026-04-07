package com.bandchu.api.domain.shorts.dto

import java.time.OffsetDateTime


// 댓글 응답용
data class ShortsCommentResponse(
    val commentId: Long,
    val nickname: String,
    val content: String,
    val createdAt: OffsetDateTime
)