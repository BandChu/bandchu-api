package com.bandchu.api.domain.concert.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

data class ConcertSchedule(
    @get:Schema(description = "콘서트 스케쥴 고유 id", example = "1111")
    val id: Long,
    @get:Schema(description = "", example = "콘서트 아이디, 제목 등등의 리스트")
    val date: OffsetDateTime,
    @get:Schema(description = "콘서트 고유 ID", example = "11")
    val concertId: Long
)
