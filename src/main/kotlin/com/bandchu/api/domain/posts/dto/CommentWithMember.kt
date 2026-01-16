package com.bandchu.api.domain.posts.dto

import java.time.OffsetDateTime

data class CommentWithMember(
    val commentId: Long,
    val postId: Long,
    val memberId: Long,
    val memberName: String,
    val content: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)
