package com.bandchu.api.domain.album.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.net.URI
import java.time.OffsetDateTime
@Schema(description = "앨범 상세 정보")
data class AlbumDetailResponse(
    @get:Schema(description = "앨범 Id", example = "101")
    val albumId: Long,

    @get:Schema(description = "앨범 이름", example = "HEADACHE(데이먼스 이어 앨범입니다)")
    val name: String,

    @get:Schema(description = "앨범 커버 이미지 url", example = "https://i.namu.wiki/i/kvPCkr4J7w8GsrVNs334lPORAGVtsuIhTuJRzIAR8NpG4nYJFgqwDi_W9qcp6EJDekFEUkCEKpzLTSN3fsdNWQ.webp")
    val coverImageUrl: String?,

    @get:Schema(description = "앨범 발매 날짜 ", example = "2026년 1월 9일")
    val releaseDate: String,

    @get:Schema(description = "앨범 상세 설명", example = "데이먼스이어 첫 EP 앨범입니다.")
    val description: String?,

    @get:Schema(description = "앨범 내 수록곡들", example = "cherry, 너의기사, ai 등등의 노래")
    val tracks: List<TrackResponse>
)
