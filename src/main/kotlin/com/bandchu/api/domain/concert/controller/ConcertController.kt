package com.bandchu.api.domain.concert.controller

import com.bandchu.api.domain.concert.dto.request.ConcertCreateRequest
import com.bandchu.api.domain.concert.dto.request.ConcertUpdateRequest
import com.bandchu.api.domain.concert.dto.response.ConcertDetailResponse
import com.bandchu.api.domain.concert.dto.response.ConcertListResponse
import com.bandchu.api.domain.concert.dto.response.ConcertSubscribedResponse
import com.bandchu.api.domain.concert.dto.toCommand
import com.bandchu.api.domain.concert.dto.toConcertDetailResponse
import com.bandchu.api.domain.concert.dto.toConcertListResponse
import com.bandchu.api.domain.concert.dto.toConcertSubscribedResponse
import com.bandchu.api.domain.concert.service.ConcertService
import com.bandchu.api.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/concerts")
@Tag(name = "Concert", description = "콘서트 관련 API")
class ConcertController(
    private val concertService: ConcertService
) {
    @Operation(summary = "모든 콘서트 정보 가져오기", description = "한 artist에 대한 모든 콘서트 정보 가져오기")
    @GetMapping("")
    fun getAll(
        @RequestParam artistId: Long
    ): ResponseEntity<ApiResponse<ConcertListResponse>> {
        val concerts = concertService.getAllByArtist(artistId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(concerts.toConcertListResponse()))
    }
    @Operation(summary = "콘서트 세부정보 가져오기", description = "하나의 콘서트에 대한 상세 정보 가져오기 conertId로 가져옴")
    @GetMapping("/{concertId}")
    fun getDetail(
        @PathVariable concertId: Long
    ): ResponseEntity<ApiResponse<ConcertDetailResponse>> {
        val concert = concertService.getDetail(concertId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(concert.toConcertDetailResponse()))
    }

    @Operation(summary = "구독한 아티스트의 콘서트 정보 가져오기", description = "구독한 정보를 가지고 콘서트를 가지고 옵니다. 사용자가 구독한 모든 아티스트들에 대한 콘서트 정보들을 가져옵니다.")
    @GetMapping("/subscribed")
    fun getSubscribed(): ResponseEntity<ApiResponse<ConcertSubscribedResponse>> {
        val concerts = concertService.getSubscribed()

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(concerts.toConcertSubscribedResponse()))
    }
    @Operation(summary = "콘서트 일정 생성하기", description = "새로운 콘서트 일정, 계획에 대해 생성합니다.")
    @PostMapping("")
    fun create(
        @RequestBody @Valid request: ConcertCreateRequest
    ): ResponseEntity<ApiResponse<ConcertDetailResponse>> {
        val concert = concertService.create(request.toCommand())

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(concert.toConcertDetailResponse()))
    }
    @Operation(summary = "콘서트 업데이트하기", description = "콘서트 ID 기반으로 업데이트합니다.")
    @PatchMapping("/{concertId}")
    fun update(
        @PathVariable concertId: Long,
        @RequestBody @Valid request: ConcertUpdateRequest
    ): ResponseEntity<ApiResponse<ConcertDetailResponse>> {
        val concert = concertService.update(request.toCommand(concertId))

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(concert.toConcertDetailResponse()))
    }
    @Operation(summary = "콘서트 삭제", description = "콘서트 ID 기반으로 기존에 있던 콘서트 일정을 삭제합니다.")
    @DeleteMapping("/{concertId}")
    fun delete(
        @PathVariable concertId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        concertService.delete(concertId)

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .body(ApiResponse.success(message = "공연 삭제가 완료되었습니다."))
    }
}