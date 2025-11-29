package com.bandchu.api.domain.concert.dto

import com.bandchu.api.domain.concert.dto.request.ConcertCreateRequest
import com.bandchu.api.domain.concert.dto.request.ConcertUpdateRequest
import com.bandchu.api.domain.concert.dto.response.ConcertDetailResponse
import com.bandchu.api.domain.concert.dto.response.ConcertSubscribedResponse
import com.bandchu.api.domain.concert.model.Concert
import com.bandchu.api.domain.concert.model.ConcertSchedule
import com.bandchu.api.domain.concert.service.dto.ConcertScheduleCommand
import com.bandchu.api.domain.concert.service.dto.ConcertSubscribedRead
import com.bandchu.api.domain.concert.service.dto.CreateConcertCommand
import com.bandchu.api.domain.concert.service.dto.UpdateConcertCommand
import java.net.URI
import java.time.OffsetDateTime

/**
 *  Domain Model → Web Response
 */

/* 공연 상세 조회 */
/* 공연 생성 */
/* 공연 수정 */
fun ConcertSchedule.toPerformingScheduleDto(): PerformingScheduleDto =
    PerformingScheduleDto(
        date = date.toString()
    )

fun Concert.toConcertDetailResponse(): ConcertDetailResponse =
    ConcertDetailResponse(
        concertId = id,
        title = title,
        place = place,
        posterImageUrl = posterImageUrl?.toString(),
        information = information,
        bookingUrl = bookingUrl?.toString(),
        bookingSchedule = bookingSchedule.toString(),
        performingSchedule = schedules.map { it.toPerformingScheduleDto() },
        createdAt = createdAt.toString()
    )

/* 구독한 아티스트의 공연 조회 */
fun Concert.toSubscribedConcertDto(): SubscribedConcertDto =
    SubscribedConcertDto(
        concertId = this.id,
        title = this.title,
        place = this.place,
        bookingSchedule = this.bookingSchedule.toString(),
        performingSchedule = this.schedules.map { it.toPerformingScheduleDto() }
    )
fun List<ConcertSubscribedRead>.toConcertSubscribedResponse(): ConcertSubscribedResponse {
    val subscribedArtistResponses = this.map { readModel ->

        val concertResponses = readModel.concerts.map { it.toSubscribedConcertDto() }

        SubscribedArtistDto(
            artistId = readModel.artists.id,
            name = readModel.artists.artistName,
            profileImageUrl = readModel.artists.profileImageUrl,
            subscribedAt = readModel.subscribedAt.toString(),
            concerts = concertResponses
        )
    }

    return ConcertSubscribedResponse(artists = subscribedArtistResponses)
}

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