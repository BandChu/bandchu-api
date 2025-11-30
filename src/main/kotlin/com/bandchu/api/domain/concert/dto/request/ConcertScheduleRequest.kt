package com.bandchu.api.domain.concert.dto.request

import jakarta.validation.constraints.NotBlank

data class ConcertScheduleRequest(
    @field:NotBlank("공연 날짜를 입력하세요.")
    val date: String
)