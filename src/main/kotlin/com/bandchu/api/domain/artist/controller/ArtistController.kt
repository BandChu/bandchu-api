package com.bandchu.api.domain.artist.controller

import com.bandchu.api.domain.artist.dto.request.ArtistSearchCondition
import com.bandchu.api.domain.artist.dto.response.ArtistListResponse
import com.bandchu.api.domain.artist.dto.response.ArtistSearchResponse
import com.bandchu.api.domain.artist.dto.toArtistListResponse
import com.bandchu.api.domain.artist.dto.toSearchResponse
import com.bandchu.api.domain.artist.service.ArtistService
import com.bandchu.api.global.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/artists")
class ArtistController(
    private val artistService: ArtistService
) {

    @GetMapping("")
    fun getAll(): ResponseEntity<ApiResponse<ArtistListResponse>> {
        val artists = artistService.getAll()

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(artists.toArtistListResponse()))
    }

    @GetMapping("/search")
    fun search(
        @ModelAttribute searchCondition: ArtistSearchCondition
    ): ResponseEntity<ApiResponse<ArtistSearchResponse>> {
        val searchResults = artistService.search(searchCondition.keyword)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(searchResults.toSearchResponse()))
    }
}