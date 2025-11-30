package com.bandchu.api.domain.album.controller

import com.bandchu.api.domain.album.dto.request.AlbumCreateRequest
import com.bandchu.api.domain.album.dto.response.AlbumDetailResponse
import com.bandchu.api.domain.album.dto.toAlbumDetailResponse
import com.bandchu.api.domain.album.dto.toCommand
import com.bandchu.api.domain.album.service.AlbumService
import com.bandchu.api.global.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/albums")
class AlbumController(
    private val albumService: AlbumService
) {
    @GetMapping("/{albumId}")
    fun getDetail(
        @PathVariable albumId: Long
    ): ResponseEntity<ApiResponse<AlbumDetailResponse>> {
        val album = albumService.getDetail(albumId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(album.toAlbumDetailResponse()))
    }

    @PostMapping("")
    fun create(
        @RequestBody @Valid request: AlbumCreateRequest
    ): ResponseEntity<ApiResponse<AlbumDetailResponse>> {
        val album = albumService.create(request.toCommand())

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(album.toAlbumDetailResponse()))
    }

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