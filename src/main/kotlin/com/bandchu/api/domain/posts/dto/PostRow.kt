package com.bandchu.api.domain.posts.dto

import com.bandchu.api.domain.posts.table.PostType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

data class PostRow (
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val id: Long,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val memberId: Long,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val type: PostType,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val title: String,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val content: String,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val createdAt: OffsetDateTime,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val updatedAt: OffsetDateTime
)