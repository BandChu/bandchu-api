package com.bandchu.api.domain.artist.dto.response

import com.bandchu.api.domain.artist.dto.ArtistSearchResultDto
import com.bandchu.api.domain.artist.dto.ConcertSearchResultDto
import io.swagger.v3.oas.annotations.media.Schema
@Schema(description = "앨범 상세 정보")
data class ArtistSearchResponse(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val artists: List<ArtistSearchResultDto>,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val concerts: List<ConcertSearchResultDto>
)
