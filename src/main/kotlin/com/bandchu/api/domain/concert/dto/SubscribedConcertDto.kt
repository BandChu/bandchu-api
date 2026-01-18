package com.bandchu.api.domain.concert.dto

import com.bandchu.api.domain.concert.dto.response.ConcertScheduleResponse
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "구독 중인 콘서트 정보")
data class SubscribedConcertDto(
    @get:Schema(description = "콘서트 고유 ID", example = "101")
    val concertId: Long,
    @get:Schema(description = "콘서트 타이틀", example = "펜타포트 2026")
    val title: String,
    @get:Schema(description = "콘서트 장소", example = "인천 인스파이어 아레나")
    val place: String,
    @get:Schema(description = "예매 url", example = "http://www.interpark.com")
    val bookingUrl: String?,
    @get:Schema(description = "콘서트 포스터 이미지 url", example = "http://www.concert.poster.com")
    val posterImageUrl: String?,
    @get:Schema(description = "공연 스케쥴", example = "공연 날짜 11월 1일, 11월 2일, 11월 3일 이런식으로 여러 날짜 리스트로 담기")
    val performingSchedule: List<ConcertScheduleResponse>,
    @get:Schema(description = "예약 스케쥴", example = "예약 날짜 11월 1일 오후 1시 에약 ")
    val bookingSchedule: String?
)
