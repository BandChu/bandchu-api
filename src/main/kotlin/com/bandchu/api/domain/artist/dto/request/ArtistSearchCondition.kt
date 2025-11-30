package com.bandchu.api.domain.artist.dto.request

import jakarta.validation.constraints.NotBlank

data class ArtistSearchCondition(
    @field:NotBlank("검색어를 입력하세요.")
    val keyword: String,
)
