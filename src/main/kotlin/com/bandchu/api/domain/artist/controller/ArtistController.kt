package com.bandchu.api.domain.artist.controller

import com.bandchu.api.domain.artist.dto.request.ArtistCreateRequest
import com.bandchu.api.domain.artist.dto.request.ArtistSearchCondition
import com.bandchu.api.domain.artist.dto.request.ArtistUpdateRequest
import com.bandchu.api.domain.artist.dto.response.ArtistDetailResponse
import com.bandchu.api.domain.artist.dto.response.ArtistListResponse
import com.bandchu.api.domain.artist.dto.response.ArtistMeResponse
import com.bandchu.api.domain.artist.dto.response.ArtistSearchResponse
import com.bandchu.api.domain.artist.dto.toArtistDetailResponse
import com.bandchu.api.domain.artist.dto.toArtistListResponse
import com.bandchu.api.domain.artist.dto.toCommand
import com.bandchu.api.domain.artist.dto.toSearchResponse
import com.bandchu.api.domain.artist.service.ArtistService
import com.bandchu.api.global.response.ApiResponse
import com.bandchu.api.global.util.getCurrentUserId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/artists")
class ArtistController(
    private val artistService: ArtistService
) {

    @GetMapping("/me")
    fun getMe(): ResponseEntity<ApiResponse<ArtistMeResponse>> {
        val result = artistService.getMyDetail(getCurrentUserId())
        // TODO: MVP 이후, 앨범 및 공연 조회 API를 분리할 수 있을까? 무한 스크롤 페이지네이션 필요할 것 같은데

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(result))
    }

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

    @GetMapping("/{artistId}")
    fun getDetail(
        @PathVariable artistId: Long
    ): ResponseEntity<ApiResponse<ArtistDetailResponse>> {
        val artist = artistService.getDetail(artistId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(artist.toArtistDetailResponse()))
    }

    @PostMapping("")
    fun create(
        @RequestBody request: ArtistCreateRequest
    ): ResponseEntity<ApiResponse<ArtistDetailResponse>> {
        val artist = artistService.createDetail(request.toCommand())

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(artist.toArtistDetailResponse()))
    }

    @PatchMapping("/{artistId}")
    fun updateDetail(
        @PathVariable artistId: Long,
        @RequestBody request: ArtistUpdateRequest
    ): ResponseEntity<ApiResponse<ArtistDetailResponse>> {
        val artist = artistService.updateDetail(request.toCommand(artistId))

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(artist.toArtistDetailResponse()))
    }
}