package com.bandchu.api.domain.posts.dto.response

import com.bandchu.api.domain.posts.table.CommentTable
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.OffsetDateTime

data class CommentResponse(
    val memberId: Long,
    var memberName: String? = null,
    val postId: Long,
    val commentId: Long,

    val content: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

fun ResultRow.toCommentResponse() :CommentResponse {
    return CommentResponse(
        memberId = this[CommentTable.memberId].value,
        postId = this[CommentTable.postId],
        commentId = this[CommentTable.id],
        
        content = this[CommentTable.content],
        createdAt = this[CommentTable.createdAt],
        updatedAt = this[CommentTable.updatedAt]
    )
}