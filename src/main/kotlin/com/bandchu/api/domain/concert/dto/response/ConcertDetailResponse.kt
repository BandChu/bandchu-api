package com.bandchu.api.domain.concert.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal


@Schema(description = "콘서트 세부 사항 응답")
data class ConcertDetailResponse(
    @get:Schema(description = "콘서트 고유 ID", example = "101")
    val concertId: Long,
    @get:Schema(description = "콘서트 제목", example = "펜타포트 2025")
    val title: String,
    @get:Schema(description = "콘서트 장소", example = "인천 인스파이어 아레나")
    val place: String,
    @get:Schema(description = "위도 (클라이언트가 등록 시 전달)", example = "37.5665")
    val latitude: BigDecimal?,
    @get:Schema(description = "경도 (클라이언트가 등록 시 전달)", example = "126.9780")
    val longitude: BigDecimal?,
    @get:Schema(description = "포스터 이미지 url", example = "http://www.image.co,.kr")
    val posterImageUrl: String?,
    @get:Schema(description = "콘서트 관련 정보", example = "이번 콘서트는 올해 하반기 최대 규모 락 밴드 페스티벌입니다.")
    val information: String?,
    @get:Schema(description = "콘서트 예약 날짜", example = "예약날짜 2026년 11월 1일 오후 1시 예약")
    val bookingSchedule: String?,
    @get:Schema(description = "콘서트 예약 url", example = "http://interpark.ck.kr")
    val bookingUrl: String?,
    @get:Schema(description = "콘서트 공연 스케쥴", example = "11월 5일 11월 6일 11월 7일")
    val performingSchedule: List<ConcertScheduleResponse> = emptyList(),
    @get:Schema(description = "이 객체 생성 시각", example = "2024:11:00")
    val createdAt: String
)
