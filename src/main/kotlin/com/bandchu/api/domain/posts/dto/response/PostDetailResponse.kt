package com.bandchu.api.domain.posts.dto.response


data class MediaResponse(
    val mediaId: Long,
    val s3Url: String,
    val fileSize: String
)

data class CommentResponse(
    val commentId: Long,
    val content: String,
    val createdAt: String
)

data class LikeResponse(
    val likeId: Long,
    val commentId: Long,
    val createdAt: String
)

data class PostDetailResponse(
    val postId: Long,
    val artistId: Long,
    val postType: String,
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val media: List<MediaResponse>,
    val comments: List<CommentResponse>
)

data class ReportResponse(
    val reportId: Long,
    val postId: Long,
    val createdAt: String,
)