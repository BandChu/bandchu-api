package com.bandchu.api.domain.artist.dto.response

import com.bandchu.api.domain.artist.dto.ArtistSnsDto
import io.swagger.v3.oas.annotations.media.Schema
@Schema(description = "앨범 상세 정보")
data class ArtistDetailResponse(
    @get:Schema(description = "아티스트 고유 ID", example = "111")
    val artistId: Long,

    @get:Schema(description = "아티스트 이름", example = "데이먼스이어")
    val name: String,

    @get:Schema(description = "프로필 이미지 url", example = "https:\\/\\/www.example.com")
    val profileImageUrl: String?,

    @get:Schema(description = "아티스트 설명", example = "데이먼스이어는 yours, cherry, busan, 너의 기사 등의 대표곡들이 존재하는 가수입니다.")
    val description: String?,

    @get:Schema(description = "아티스트 장르", example = "INDIE")
    val genre: List<String>,

    @get:Schema(description = "아티스트 sns 링크 모음 ", example = "페이스북 링크, 인스타그램 링크 등등")
    val sns: List<ArtistSnsDto>
)
