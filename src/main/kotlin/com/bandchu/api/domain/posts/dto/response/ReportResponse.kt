package com.bandchu.api.domain.posts.dto.response

data class ReportResponse(
    val reportId: Long,
    val postId: Long,
    val createdAt: String,
)
