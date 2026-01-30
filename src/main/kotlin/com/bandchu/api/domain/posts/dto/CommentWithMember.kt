package com.bandchu.api.domain.posts.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "콘서트 디테일 리스트 응답")
data class CommentWithMember(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val commentId: Long,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val postId: Long,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val memberId: Long,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val memberName: String,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val content: String,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val createdAt: OffsetDateTime,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val updatedAt: OffsetDateTime
)
