package com.bandchu.api.domain.album.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AlbumCreateRequest(
    @field:NotBlank(message = "앨범명을 입력하세요.")
    @field:Size(max = 30, message = "앨범명은 최대 30자까지 입력 가능합니다.")
    val name: String,
    val coverImageUrl: String?,
    @field:NotBlank(message = "발매일은 필수 입력 항목입니다.")
    val releaseDate: String,
    val description: String?,
    @field:Valid
    val tracks: List<TrackRequest> = emptyList()
)
