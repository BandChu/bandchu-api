package com.bandchu.api.domain.artist.dto.request

import com.bandchu.api.domain.artist.dto.ArtistSnsDto
import com.bandchu.api.domain.artist.model.Genre
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

data class ArtistCreateRequest(
    @field:NotBlank("아티스트 이름을 입력하세요.")
    val name: String,
    val profileImageUrl: String?,
    val description: String?,
    val genre: List<Genre> = emptyList(),
    @field:Valid
    val sns: List<ArtistSnsDto> = emptyList()
)
