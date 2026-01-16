package com.bandchu.api.domain.posts.dto.response

import com.bandchu.api.domain.posts.dto.PostListItem

data class PostListResponse(
    var posts: List<PostListItem>,
    val totalElements: Long,
    val totalPages: Int
)