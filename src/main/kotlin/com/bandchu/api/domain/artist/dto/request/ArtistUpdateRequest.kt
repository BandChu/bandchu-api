package com.bandchu.api.domain.artist.dto.request

import com.bandchu.api.domain.artist.dto.ArtistSnsDto
import com.bandchu.api.domain.artist.model.Genre
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
@Schema(description = "앨범 상세 정보")
data class ArtistUpdateRequest(
    @get:Schema(description = "아티스트 이름", example = "VAUNDY")
    @field:NotBlank("아티스트 이름을 입력하세요.")
    val name: String,

    @get:Schema(description = "프로필 이미지 url", example = "http://s3://")
    val profileImageUrl: String?,

    @get:Schema(description = "아티스트 설명", example = "VAUNDY는 JPOP의 신예입니다.")
    val description: String?,

    @get:Schema(description = "아티스트 장르", example = "JPOP")
    val genre: List<Genre> = emptyList(),

    @get:Schema(description = "아티스트 sns 링크 모음", example = "인스타그램 링크, 페이스북 링크 등등")
    @field:Valid
    val sns: List<ArtistSnsDto> = emptyList()
)
