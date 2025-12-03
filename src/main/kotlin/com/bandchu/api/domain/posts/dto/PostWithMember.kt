package com.bandchu.api.domain.posts.dto

import com.bandchu.api.domain.posts.table.PostType
import java.time.OffsetDateTime

data class PostWithMember(
    val postId: Long,
    val postType: PostType,
    val title: String,
    val content: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val memberId: Long,
    val memberName: String,
)