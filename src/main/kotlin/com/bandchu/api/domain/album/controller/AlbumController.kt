package com.bandchu.api.domain.album.controller

import com.bandchu.api.domain.album.dto.request.AlbumCreateRequest
import com.bandchu.api.domain.album.dto.response.AlbumDetailResponse
import com.bandchu.api.domain.album.dto.response.AlbumListResponse
import com.bandchu.api.domain.album.dto.toAlbumDetailResponse
import com.bandchu.api.domain.album.dto.toAlbumListResponse
import com.bandchu.api.domain.album.dto.toCommand
import com.bandchu.api.domain.album.service.AlbumService
import com.bandchu.api.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/albums")
@Tag(name = "Album", description = "앨범 관련 API")
class AlbumController(
    private val albumService: AlbumService
) {
    @Operation(summary = "앨범 전체 조회", description = "특정 아티스트의 모든 앨범을 가져옵니다.")
    @GetMapping("")
    fun getAll(
        @RequestParam artistId: Long
    ): ResponseEntity<ApiResponse<AlbumListResponse>> {
        val albums = albumService.getAllByArtist(artistId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(albums.toAlbumListResponse()))
    }
    @Operation(summary = "앨범 상세 조회", description = "앨범 ID 기준으로 세부 정보 가지고옵니다")
    @GetMapping("/{albumId}")
    fun getDetail(
        @PathVariable albumId: Long
    ): ResponseEntity<ApiResponse<AlbumDetailResponse>> {
        val album = albumService.getDetail(albumId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(album.toAlbumDetailResponse()))
    }
    @Operation(summary = "앨범 생성", description = "특정 아티스트가 앨범을 생성합니다")
    @PostMapping("")
    fun create(
        @RequestBody @Valid request: AlbumCreateRequest
    ): ResponseEntity<ApiResponse<AlbumDetailResponse>> {
        val album = albumService.create(request.toCommand())

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(album.toAlbumDetailResponse()))
    }
    @Operation(summary = "앨범 일부 삭제", description = "앨범을 앨범 ID를 통해 삭제합니다")
    @DeleteMapping("/{albumId}")
    fun delete(
        @PathVariable albumId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        albumService.delete(albumId)

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .body(ApiResponse.success("앨범 삭제가 완료되었습니다."))
    }
}