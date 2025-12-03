package com.bandchu.api.domain.concert.dto

import com.bandchu.api.domain.concert.dto.request.ConcertCreateRequest
import com.bandchu.api.domain.concert.dto.request.ConcertScheduleRequest
import com.bandchu.api.domain.concert.dto.request.ConcertUpdateRequest
import com.bandchu.api.domain.concert.dto.response.ConcertDetailResponse
import com.bandchu.api.domain.concert.dto.response.ConcertListResponse
import com.bandchu.api.domain.concert.dto.response.ConcertScheduleResponse
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

/* 특정 아티스트의 공연 목록 조회 */
fun List<Concert>.toConcertListResponse(): ConcertListResponse =
    ConcertListResponse(
        concerts = this.map { it.toConcertDetailResponse() }
    )

/* 공연 상세 조회 */
/* 공연 생성 */
/* 공연 수정 */
fun ConcertSchedule.toConcertScheduleResponse(): ConcertScheduleResponse =
    ConcertScheduleResponse(
        id = id,
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
        performingSchedule = schedules.map { it.toConcertScheduleResponse() },
        createdAt = createdAt.toString()
    )

/* 구독한 아티스트의 공연 조회 */
fun Concert.toSubscribedConcertDto(): SubscribedConcertDto =
    SubscribedConcertDto(
        concertId = this.id,
        title = this.title,
        place = this.place,
        bookingSchedule = this.bookingSchedule.toString(),
        bookingUrl = this.bookingUrl.toString(),
        posterImageUrl = this.posterImageUrl.toString(),
        performingSchedule = this.schedules.map { it.toConcertScheduleResponse() }
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
fun ConcertScheduleRequest.toCommand(): ConcertScheduleCommand =
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