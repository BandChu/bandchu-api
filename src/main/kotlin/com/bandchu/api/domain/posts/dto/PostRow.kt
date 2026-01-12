package com.bandchu.api.domain.posts.dto

import com.bandchu.api.domain.posts.table.PostType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "게시글 줄")
data class PostRow (
    @get:Schema(description = "postrow의 고유 ID", example = "101")
    val id: Long,

    @get:Schema(description = "멤버의 고유 ID", example = "102")
    val memberId: Long,

    @get:Schema(description = "게시글 타입", example = "FREE")
    val type: PostType,

    @get:Schema(description = "게시글 제목", example = "리도어 실물 본썰")
    val title: String,

    @get:Schema(description = "게시글 내용", example = "리도어 보고싶다")
    val content: String,

    @get:Schema(description = "게시글 생성 날짜", example = "2022:02:02")
    val createdAt: OffsetDateTime,

    @get:Schema(description = "게시글 수정 날짜", example = "2023:03:03")
    val updatedAt: OffsetDateTime
)