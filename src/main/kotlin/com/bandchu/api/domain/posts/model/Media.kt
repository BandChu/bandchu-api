package com.bandchu.api.domain.posts.model

data class Media (
    val mediaId: Int? = null,
    val postId: Long,
    val s3Url: String,
    val fileSize: String,
    val createdAt: kotlinx.datetime.LocalDateTime,
    val updatedAt: kotlinx.datetime.LocalDateTime
    )