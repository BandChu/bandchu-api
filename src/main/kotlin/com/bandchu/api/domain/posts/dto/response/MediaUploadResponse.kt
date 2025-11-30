package com.bandchu.api.domain.posts.dto.response

data class MediaUploadResponse(
    val mediaId: Long,
    val postId: Long,
    val artistId: Long,
    val s3Url: String,
    val fileSize: String
)
