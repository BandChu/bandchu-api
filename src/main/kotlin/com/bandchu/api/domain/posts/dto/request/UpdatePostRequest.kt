package com.bandchu.api.domain.posts.dto.request

import io.swagger.v3.oas.annotations.media.Schema

data class UpdatePostRequest(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val title: String,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val content: String
)