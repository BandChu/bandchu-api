package com.bandchu.api.domain.artist.controller

import com.bandchu.api.domain.artist.dto.response.ArtistListResponse
import com.bandchu.api.domain.artist.dto.toArtistListResponse
import com.bandchu.api.domain.artist.service.ArtistService
import com.bandchu.api.global.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/artists")
class ArtistController(
    private val artistService: ArtistService
) {

    @GetMapping("")
    fun getAll(): ApiResponse<ArtistListResponse> {
        val artists = artistService.getAll()

        return ApiResponse.success(artists.toArtistListResponse())
    }
}