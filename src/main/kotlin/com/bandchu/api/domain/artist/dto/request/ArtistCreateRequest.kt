package com.bandchu.api.domain.artist.dto.request

import com.bandchu.api.domain.artist.dto.ArtistSnsDto
import com.bandchu.api.domain.artist.model.Genre
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
@Schema(description = "앨범 상세 정보")
data class ArtistCreateRequest(
    @get:Schema(description = "artist 이름", example = "아이유")
    @field:NotBlank("아티스트 이름을 입력하세요.")
    val name: String,

    @get:Schema(description = "프로필 이미지 url ", example = "http://s3:///")
    val profileImageUrl: String?,

    @get:Schema(description = "아티스트 설명", example = "아이유는 21세기 한국을 대표하는 여자 솔로 가수입니다")
    val description: String?,

    @get:Schema(description = "아티스트 장르", example = "KPOP 혹은 BALLAD 등등")
    val genre: List<Genre> = emptyList(),

    @get:Schema(description = "아티스트 SNS DTO", example = "인스타그램 링크, 사클 링크 등등")
    @field:Valid
    val sns: List<ArtistSnsDto> = emptyList()
)
