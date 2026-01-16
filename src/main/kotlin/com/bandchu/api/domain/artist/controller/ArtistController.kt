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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/artists")
@Tag(name = "Artist", description = "아티스트 관련 API")
class ArtistController(
    private val artistService: ArtistService
) {
    @Operation(summary = "아티스트 본인의 정보 가져오기", description = "아티스트 본인의 정보를 가져옵니다 ex : 앨범, 콘서트, 아티스트 정보 가져오기")
    @GetMapping("/me")
    fun getMe(): ResponseEntity<ApiResponse<ArtistMeResponse>> {
        val result = artistService.getMyDetail(getCurrentUserId())
        // TODO: MVP 이후, 앨범 및 공연 조회 API를 분리할 수 있을까? 무한 스크롤 페이지네이션 필요할 것 같은데

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(result))
    }
    @Operation(summary = "아티스트 프로필 가져오기", description = "아티스트 프로필을 가져옵니다.")
    @GetMapping("")
    fun getAll(): ResponseEntity<ApiResponse<ArtistListResponse>> {
        val artists = artistService.getAll()

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(artists.toArtistListResponse()))
    }
    @Operation(summary = "앨범 일부 삭제", description = "앨범을 앨범 ID를 통해 삭제합니다")
    @GetMapping("/search")
    fun search(
        @ModelAttribute searchCondition: ArtistSearchCondition
    ): ResponseEntity<ApiResponse<ArtistSearchResponse>> {
        val searchResults = artistService.search(searchCondition.keyword)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(searchResults.toSearchResponse()))
    }
    @Operation(summary = "앨범 일부 삭제", description = "앨범을 앨범 ID를 통해 삭제합니다")
    @GetMapping("/{artistId}")
    fun getDetail(
        @PathVariable artistId: Long
    ): ResponseEntity<ApiResponse<ArtistDetailResponse>> {
        val artist = artistService.getDetail(artistId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(artist.toArtistDetailResponse()))
    }
    @Operation(summary = "아티프로필 생성하기", description = "아티프로필을 생성합니다.")
    @PostMapping("")
    fun create(
        @RequestBody request: ArtistCreateRequest
    ): ResponseEntity<ApiResponse<ArtistDetailResponse>> {
        val artist = artistService.createDetail(request.toCommand())

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(artist.toArtistDetailResponse()))
    }
    @Operation(summary = "아티프로필 업데이트", description = "아티프로필을 업데이트합니다.")
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