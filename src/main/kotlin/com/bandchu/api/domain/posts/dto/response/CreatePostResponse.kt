package com.bandchu.api.domain.posts.dto.response

import com.bandchu.api.domain.posts.table.PostTable
import com.bandchu.api.domain.posts.table.PostType
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.OffsetDateTime

data class CreatePostResponse(
    val id: Long,
    val memberId: Long,
    val type: PostType,
    var title: String,
    val content: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

fun ResultRow.toPostResponse(): CreatePostResponse = CreatePostResponse(
    id = this[PostTable.id],
    memberId = this[PostTable.memberId],
    type = this[PostTable.postType],
    title = this[PostTable.title],
    content = this[PostTable.content],
    createdAt = this[PostTable.createdAt],
    updatedAt = this[PostTable.updatedAt]
)
