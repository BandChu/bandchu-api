package com.bandchu.api.domain.posts.dto

import com.bandchu.api.domain.posts.table.PostType
import java.time.OffsetDateTime

data class PostRow (
    val id: Long,
    val memberId: Long,
    val type: PostType,
    val title: String,
    val content: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)