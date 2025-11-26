package com.bandchu.api.domain.artist.dto

import jakarta.validation.constraints.NotBlank

data class ArtistSnsDto(
    @field:NotBlank(message = "SNS 플랫폼의 종류를 입력하세요.")
    val platform: String,
    @field:NotBlank(message = "SNS URL을 입력하세요.")
    val url: String
)
