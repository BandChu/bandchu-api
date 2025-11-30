package com.bandchu.api.domain.posts.dto.response


data class PostListItem(
    val postId: Int,
    val postType: String,
    val title: String,
    val createdAt: String,
    val updatedAt: String
)

data class PostListResponse(
    val posts: List<PostListItem>,
    val totalElements: Long,
    val totalPages: Int
)