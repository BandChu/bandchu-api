package com.bandchu.api.domain.posts.dto.response
data class CreatedPostResponse(
    val postId: Int,
    val postType: String,
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String
)