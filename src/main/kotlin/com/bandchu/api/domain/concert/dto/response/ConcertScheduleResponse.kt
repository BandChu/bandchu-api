package com.bandchu.api.domain.concert.dto.response

import io.swagger.v3.oas.annotations.media.Schema


@Schema(description = "콘서트 스케쥴 응답")
data class ConcertScheduleResponse(
    @get:Schema(description = "콘서트 스케쥴 응답", example = "콘서트")
    val id: Long,
    @get:Schema(description = "콘서트 디테일 리스트 응답", example = "콘서트 아이디, 제목 등등의 리스트")
    val date: String
)
