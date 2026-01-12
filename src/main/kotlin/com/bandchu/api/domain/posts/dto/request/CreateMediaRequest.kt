package com.bandchu.api.domain.posts.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "게시판 내 미디어 생성 요청")
data class CreateMediaRequest(
    @get:Schema(description = "업로드된 S3 파일 url", example = "s3://sth.sth.~~")
    val s3Url: String,   // 업로드된 S3 파일 URL

    @get:Schema(description = "업로드된 파일 사이즈", example = "125,000,000 byte")
    val fileSize: Long   // 파일 크기 (byte 단위)
)