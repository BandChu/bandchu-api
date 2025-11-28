package com.bandchu.api.domain.concert.controller

import com.bandchu.api.domain.concert.dto.request.ConcertCreateRequest
import com.bandchu.api.domain.concert.dto.request.ConcertUpdateRequest
import com.bandchu.api.domain.concert.dto.response.ConcertCreateResponse
import com.bandchu.api.domain.concert.dto.response.ConcertUpdateResponse
import com.bandchu.api.domain.concert.dto.toCommand
import com.bandchu.api.domain.concert.dto.toConcertCreateResponse
import com.bandchu.api.domain.concert.dto.toConcertUpdateResponse
import com.bandchu.api.domain.concert.service.ConcertService
import com.bandchu.api.global.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/api/concerts")
class ConcertController(
    private val concertService: ConcertService
) {
    @PostMapping("")
    fun create(
        @RequestBody @Valid request: ConcertCreateRequest
    ): ResponseEntity<ApiResponse<ConcertCreateResponse>> {
        val concert = concertService.create(request.toCommand())

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(concert.toConcertCreateResponse()))
    }

    @PostMapping("/{concertId}")
    fun update(
        @PathVariable concertId: Long,
        @RequestBody @Valid request: ConcertUpdateRequest
    ): ResponseEntity<ApiResponse<ConcertUpdateResponse>> {
        val concert = concertService.update(request.toCommand(concertId))

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(concert.toConcertUpdateResponse()))
    }
}