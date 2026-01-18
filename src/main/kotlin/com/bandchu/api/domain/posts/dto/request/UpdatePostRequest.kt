package com.bandchu.api.domain.posts.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "게시판 글 업데이트, 수정하기")
data class UpdatePostRequest(
    @get:Schema(description = "업데이트, 수정할 게시판 글 제목", example = "리도어 실물 본 썰 푼다.")
    val title: String,

    @get:Schema(description = "업데이트, 수정할 게시판 글 내용", example = "이등대 왤케 잘생김 말이 안돼 노래도 잘해 잘생겼어 기타도 잘쳐 작곡도 잘해 키도 커 ")
    val content: String
)