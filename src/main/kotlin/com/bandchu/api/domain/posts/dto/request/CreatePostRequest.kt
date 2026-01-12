package com.bandchu.api.domain.posts.dto.request

import com.bandchu.api.domain.posts.table.PostType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "게시판 글 생성 요청")
data class CreatePostRequest(
    @get:Schema(description = "게시판 종류", example = "자유 게시판 (FREE)")
    val postType: PostType,

    @get:Schema(description = "게시판 제목", example = "리도어 실물 본 썰 풀게")
    val title: String,

    @get:Schema(description = "글 내용", example = "와 이등대 개잘생겼어 말 안돼 어떻게 노래도 잘해 작곡도 잘해 잘생겨 다 가졌지?")
    val content: String
)