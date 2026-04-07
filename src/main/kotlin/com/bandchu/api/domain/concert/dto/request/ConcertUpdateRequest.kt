package com.bandchu.api.domain.concert.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

@Schema(description = "콘서트 정보 업데이트 요청")
data class ConcertUpdateRequest(
    @get:Schema(description = "콘서트 이름", example = "실리카겔 단독 콘서트")
    @field:NotBlank("공연명을 입력하세요.")
    val title: String,

    @get:Schema(description = "콘서트 장소", example = "인천 인스파이어 아레나")
    @field:NotBlank("공연 장소를 입력하세요.")
    val place: String,

    @get:Schema(description = "위도 (프론트 주소/지도 검색 결과)")
    val latitude: BigDecimal? = null,

    @get:Schema(description = "경도 (프론트 주소/지도 검색 결과)")
    val longitude: BigDecimal? = null,

    @get:Schema(description = "콘서트 포스터 이미지 url", example = "http://poster.co.kr")
    val posterImageUrl: String?,

    @get:Schema(description = "콘서트 정보", example = "데이먼스 이어의 10주년 단독 공연 행사입니다.")
    val information: String?,

    @get:Schema(description = "콘서트 예약 날짜", example = "11월 1일 오후 1시")
    val bookingSchedule: String?,

    @get:Schema(description = "콘서트 예약 url", example = "http://booking.url")
    val bookingUrl: String?,

    @get:Schema(description = "콘서트 공연 하는 날짜들", example = "11월 1일, 11월 2일, 11월 3일 이런식의 날짜 리스트")
    @field:Valid
    val performingSchedule: List<ConcertScheduleRequest> = emptyList()
)
