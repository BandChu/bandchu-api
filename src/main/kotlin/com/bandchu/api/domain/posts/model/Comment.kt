package com.bandchu.api.domain.posts.model

data class Comment (
    val comment_id: Int? = null,
    val created_at: java.time.LocalDateTime,
    val updated_at: java.time.LocalDateTime,
    val content: String,
    val postId: Long
)