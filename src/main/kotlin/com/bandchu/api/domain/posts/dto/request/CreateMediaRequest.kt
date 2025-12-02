package com.bandchu.api.domain.posts.dto.request

data class CreateMediaRequest(
    val s3Url: String,   // 업로드된 S3 파일 URL
    val fileSize: Long   // 파일 크기 (byte 단위)
)