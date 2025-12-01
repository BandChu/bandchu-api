package com.bandchu.api.domain.posts.dto.request

import com.bandchu.api.domain.posts.table.PostType

data class CreatePostRequest(
    val memberId: Long,
    val postType: PostType,
    val title: String,
    val content: String
)