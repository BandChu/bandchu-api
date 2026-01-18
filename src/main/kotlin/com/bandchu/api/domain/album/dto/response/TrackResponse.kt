package com.bandchu.api.domain.album.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.net.URI
@Schema(description = "콘서트 정보 업데이트 요청")
data class TrackResponse(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val trackId: Long,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val name: String,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val url: String
)
