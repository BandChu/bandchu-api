package com.bandchu.api.domain.posts.dto.response

import com.bandchu.api.domain.posts.table.CommentTable
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.OffsetDateTime

data class CommentResponse(
    val commentId: Long,
    val content: String,
    val createdAt: OffsetDateTime
)

fun ResultRow.toCommentResponse() :CommentResponse {
    return CommentResponse(
        commentId = this[CommentTable.id],
        content = this[CommentTable.content],
        createdAt = this[CommentTable.createdAt]
    )
}