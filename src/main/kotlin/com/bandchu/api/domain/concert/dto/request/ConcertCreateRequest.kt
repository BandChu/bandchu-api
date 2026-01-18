package com.bandchu.api.domain.concert.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
@Schema(description = "콘서트 생성 요청")
data class ConcertCreateRequest(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    @field:NotBlank("공연명을 입력하세요.")
    val title: String,

    @get:Schema(description = "콘서트 공연 장소", example = "인천 인스파이어 아레나")
    @field:NotBlank("공연 장소를 입력하세요.")
    val place: String,

    @get:Schema(description = "콘서트 포스터 이미지 url", example = "http://www.co.kr")
    val posterImageUrl: String?,

    @get:Schema(description = "콘서트 정보", example = "이 콘서트는 실리카겔 10주년 기념 단독 콘서트입니다. 블라블라")
    val information: String?,

    @get:Schema(description = "콘서트 예약 날짜", example = "2025년 11월 1일 오후 1시")
    val bookingSchedule: String?,

    @get:Schema(description = "콘서트 예약 url", example = "http://booking.interpark.co.kr")
    val bookingUrl: String?,

    @get:Schema(description = "콘서트 공연 스케쥴", example = "11월 1일, 11월 2일, 11월 3일 이런식의 날짜 리스트")
    @field:Valid
    val performingSchedule: List<ConcertScheduleRequest> = emptyList()
)
