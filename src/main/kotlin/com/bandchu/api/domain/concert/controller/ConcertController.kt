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
    @Operation(summary = "", description = "앨범을 앨범 ID를 통해 삭제합니다")
    @GetMapping("")
    fun getAll(
        @RequestParam artistId: Long
    ): ResponseEntity<ApiResponse<ConcertListResponse>> {
        val concerts = concertService.getAllByArtist(artistId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(concerts.toConcertListResponse()))
    }
    @Operation(summary = "앨범 일부 삭제", description = "앨범을 앨범 ID를 통해 삭제합니다")
    @GetMapping("/{concertId}")
    fun getDetail(
        @PathVariable concertId: Long
    ): ResponseEntity<ApiResponse<ConcertDetailResponse>> {
        val concert = concertService.getDetail(concertId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(concert.toConcertDetailResponse()))
    }
    @Operation(summary = "앨범 일부 삭제", description = "앨범을 앨범 ID를 통해 삭제합니다")
    @GetMapping("/subscribed")
    fun getSubscribed(): ResponseEntity<ApiResponse<ConcertSubscribedResponse>> {
        val concerts = concertService.getSubscribed()

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(concerts.toConcertSubscribedResponse()))
    }
    @Operation(summary = "앨범 일부 삭제", description = "앨범을 앨범 ID를 통해 삭제합니다")
    @PostMapping("")
    fun create(
        @RequestBody @Valid request: ConcertCreateRequest
    ): ResponseEntity<ApiResponse<ConcertDetailResponse>> {
        val concert = concertService.create(request.toCommand())

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(concert.toConcertDetailResponse()))
    }
    @Operation(summary = "앨범 일부 삭제", description = "앨범을 앨범 ID를 통해 삭제합니다")
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
    @Operation(summary = "앨범 일부 삭제", description = "앨범을 앨범 ID를 통해 삭제합니다")
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