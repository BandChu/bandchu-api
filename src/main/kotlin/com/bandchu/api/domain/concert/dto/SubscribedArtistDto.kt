package com.bandchu.api.domain.concert.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.net.URI

@Schema(description = "구독 중인 아티스트 및 관련 콘서트 정보")
data class SubscribedArtistDto(
    @get:Schema(description = "아티스트 고유 ID", example = "101")
    val artistId: Long,

    @get:Schema(description = "아티스트 활동명", example = "아이유 (IU)")
    val name: String,

    @get:Schema(description = "아티스트 한 줄 소개", example = "대한민국의 싱어송라이터이자 배우입니다.")
    val description: String?,

    @get:Schema(description = "활동 장르 리스트", example = "[\"K-POP\", \"Ballad\"]")
    val genre: List<String>,

    @get:Schema(description = "아티스트 프로필 이미지 경로", example = "https://example.com/profiles/iu.jpg")
    val profileImageUrl: URI?,

    @get:Schema(description = "구독 시작 일시 (ISO 8601)", example = "2030-01-11T21:00:00")
    val subscribedAt: String,

    @get:Schema(description = "진행 예정인 콘서트 목록")
    val concerts: List<SubscribedConcertDto>
)
