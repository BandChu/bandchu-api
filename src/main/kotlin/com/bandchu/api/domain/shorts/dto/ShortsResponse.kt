package com.bandchu.api.domain.shorts.dto

import java.time.OffsetDateTime


// 릴스 목록 응답용
data class ShortsResponse(
    val id: Long,
    val artistName: String,
    val title: String,
    val description: String?,
    val videoUrl: String,
    val thumbnailUrl: String?,
    val viewCount: Long,
    val shareCount: Long,
    val isLiked: Boolean, // 현재 사용자가 좋아요 눌렀는지 여부
    val createdAt: OffsetDateTime
)