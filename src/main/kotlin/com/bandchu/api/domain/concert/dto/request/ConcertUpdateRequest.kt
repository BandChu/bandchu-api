package com.bandchu.api.domain.concert.dto.request

import com.bandchu.api.domain.concert.PerformingScheduleDto
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

data class ConcertUpdateRequest(
    @field:NotBlank("공연명을 입력하세요.")
    val title: String,
    @field:NotBlank("공연 장소를 입력하세요.")
    val place: String,
    val posterImageUrl: String?,
    val information: String?,
    val bookingSchedule: String?,
    val bookingUrl: String?,
    @field:Valid
    val performingSchedule: List<PerformingScheduleDto> = emptyList()
)
