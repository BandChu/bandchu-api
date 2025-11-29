package com.bandchu.api.domain.concert.dto

import jakarta.validation.constraints.NotBlank

data class PerformingScheduleDto(
    @field:NotBlank("공연 날짜를 입력하세요.")
    val date: String
)