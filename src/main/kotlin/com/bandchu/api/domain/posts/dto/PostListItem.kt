package com.bandchu.api.domain.posts.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "게시판 리스트 , 게시글 자체 말고 썸네일 같이 어떤 식으로 보여주자 할때 사용하는 객체")
data class PostListItem(
    @get:Schema(description = "post 고유 id", example = "222")
    val postId: Long,

    @get:Schema(description = "게시판 타입", example = "FREE")
    val postType: String,

    @get:Schema(description = "멤버 고유 ID", example = "101")
    val memberId: Long,

    @get:Schema(description = "멤버 이름", example = "손손손")
    var memberName: String? = null,

    @get:Schema(description = "게시글 제목", example = "리도어 실물 본 썰")
    val title: String,

    @get:Schema(description = "게시글 생성 시간", example = "2025:04:04")
    val createdAt: String,

    @get:Schema(description = "게시글 업데이트 시간", example = "2025:06:06")
    val updatedAt: String
)