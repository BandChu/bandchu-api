package com.bandchu.api.domain.posts.model

data class Post(
    val post_id: Long ,
    val title: String,
    val content: String,
    val createdAt: kotlinx.datetime.LocalDateTime,
    val updatedAt: kotlinx.datetime.LocalDateTime,
    val postType: PostType,
)

