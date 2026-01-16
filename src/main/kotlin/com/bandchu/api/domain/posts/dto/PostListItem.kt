package com.bandchu.api.domain.posts.dto

data class PostListItem(
    val postId: Long,
    val postType: String,
    val memberId: Long,
    var memberName: String? = null,
    val title: String,
    val createdAt: String,
    val updatedAt: String
)