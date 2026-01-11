package com.bandchu.api.domain.posts.dto.request

import io.swagger.v3.oas.annotations.media.Schema

data class CreateMediaRequest(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val s3Url: String,   // 업로드된 S3 파일 URL

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val fileSize: Long   // 파일 크기 (byte 단위)
)