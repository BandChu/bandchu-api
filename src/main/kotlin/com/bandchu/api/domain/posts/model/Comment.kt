package com.bandchu.api.domain.posts.model

import kotlinx.datetime.LocalDateTime

data class Comment (
    val comment_id: Int? = null,
    val created_at: LocalDateTime,
    val updated_at: LocalDateTime,
    val content: String,

)