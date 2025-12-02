package com.bandchu.api.domain.posts.dto.response

import com.bandchu.api.domain.posts.table.PostTable
import com.bandchu.api.domain.posts.table.PostType
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.OffsetDateTime

data class PostDetailResponse(
    val postId: Long,
    val artistId: Long,
    val postType: PostType,
    val title: String,
    val content: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val media: List<CreateMediaResponse>,
    val comments: List<CommentResponse>
)

fun ResultRow.toPostDetailResponse(
    media: List<CreateMediaResponse> = emptyList(),
    comments: List<CommentResponse> = emptyList()
): PostDetailResponse {
    return PostDetailResponse(
        postId = this[PostTable.id],
        artistId = this[PostTable.memberId],
        postType = this[PostTable.postType],
        title = this[PostTable.title],
        content = this[PostTable.content],
        createdAt = this[PostTable.createdAt],
        updatedAt = this[PostTable.updatedAt],
        media = media,
        comments = comments
    )
}