package com.bandchu.api.domain.posts.model

import kotlinx.datetime.LocalDateTime

data class Post(
    val post_id: Int? = null,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,

)
