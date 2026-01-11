package com.bandchu.api.domain.concert.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class ConcertScheduleRequest(

    @get:Schema(description = "공연 날짜", example = "11월 1일")

    @field:NotBlank("공연 날짜를 입력하세요.")
    val date: String
)