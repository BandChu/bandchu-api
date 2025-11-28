package com.bandchu.api.domain.concert.dto

import com.bandchu.api.domain.concert.PerformingScheduleDto
import com.bandchu.api.domain.concert.dto.request.ConcertCreateRequest
import com.bandchu.api.domain.concert.dto.request.ConcertUpdateRequest
import com.bandchu.api.domain.concert.dto.response.ConcertCreateResponse
import com.bandchu.api.domain.concert.dto.response.ConcertUpdateResponse
import com.bandchu.api.domain.concert.model.Concert
import com.bandchu.api.domain.concert.model.ConcertSchedule
import com.bandchu.api.domain.concert.service.dto.ConcertScheduleCommand
import com.bandchu.api.domain.concert.service.dto.CreateConcertCommand
import com.bandchu.api.domain.concert.service.dto.UpdateConcertCommand
import java.net.URI
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 *  Domain Model → Web Response
 */

/* 공연 생성 */
fun ConcertSchedule.toPerformingScheduleDto(): PerformingScheduleDto =
    PerformingScheduleDto(
        date = date.toString()
    )
fun Concert.toConcertCreateResponse(): ConcertCreateResponse =
    ConcertCreateResponse(
        concertId = id,
        title = title,
        place = place,
        posterImageUrl = posterImageUrl?.toString(),
        information = information,
        bookingUrl = bookingUrl?.toString(),
        bookingSchedule = bookingSchedule.toString(),
        performingSchedule = schedules.map { it.toPerformingScheduleDto() },
        createdAt = createdAt
    )

/* 공연 수정 */
fun Concert.toConcertUpdateResponse(): ConcertUpdateResponse =
    ConcertUpdateResponse(
        concertId = id,
        title = title,
        place = place,
        posterImageUrl = posterImageUrl?.toString(),
        information = information,
        bookingUrl = bookingUrl?.toString(),
        bookingSchedule = bookingSchedule.toString(),
        performingSchedule = schedules.map { it.toPerformingScheduleDto() },
        createdAt = createdAt
    )

/**
 *   Web Request → Domain Command Model
 */

/* 공연 생성 */
fun PerformingScheduleDto.toCommand(): ConcertScheduleCommand =
    ConcertScheduleCommand(
        date = OffsetDateTime.parse(date)
    )

fun ConcertCreateRequest.toCommand(): CreateConcertCommand =
    CreateConcertCommand(
        title = title,
        place = place,
        posterImageUrl = posterImageUrl?.let { URI(it) },
        information = information,
        bookingUrl = bookingUrl?.let { URI(it) },
        bookingSchedule = bookingSchedule?.let { OffsetDateTime.parse(it) },
        performingSchedule = performingSchedule.map { it.toCommand() }
    )

/* 공연 수정 */
fun ConcertUpdateRequest.toCommand(concertId: Long): UpdateConcertCommand =
    UpdateConcertCommand(
        concertId = concertId,
        title = title,
        place = place,
        posterImageUrl = posterImageUrl?.let { URI(it) },
        information = information,
        bookingUrl = bookingUrl?.let { URI(it) },
        bookingSchedule = bookingSchedule?.let { OffsetDateTime.parse(it) },
        performingSchedule = performingSchedule.map { it.toCommand() }
    )