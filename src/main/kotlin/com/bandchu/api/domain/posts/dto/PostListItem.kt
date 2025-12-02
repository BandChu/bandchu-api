package com.bandchu.api.domain.posts.dto

data class PostListItem(
    val postId: Long,
    val postType: String,
    val title: String,
    val createdAt: String,
    val updatedAt: String
)