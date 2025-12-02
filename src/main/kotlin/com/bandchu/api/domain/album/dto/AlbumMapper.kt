package com.bandchu.api.domain.album.dto

import com.bandchu.api.domain.album.dto.request.AlbumCreateRequest
import com.bandchu.api.domain.album.service.dto.CreateAlbumCommand
import com.bandchu.api.domain.album.service.dto.CreateTrackCommand
import com.bandchu.api.domain.album.dto.request.TrackRequest
import com.bandchu.api.domain.album.dto.response.AlbumDetailResponse
import com.bandchu.api.domain.album.dto.response.AlbumListResponse
import com.bandchu.api.domain.album.dto.response.AlbumSummaryResponse
import com.bandchu.api.domain.album.dto.response.TrackResponse
import com.bandchu.api.domain.album.model.Album
import com.bandchu.api.domain.album.model.Track
import java.net.URI
import java.time.OffsetDateTime

/**
 *  Domain Model → Web Response
 */

/* 특정 아티스트의 앨범 목록 조회 */
fun Album.toAlbumSummaryResponse(): AlbumSummaryResponse =
    AlbumSummaryResponse(
        albumId = id,
        name = name,
        coverImageUrl = coverImageUrl.toString(),
        releaseDate = releaseDate.toString()
    )
fun List<Album>.toAlbumListResponse(): AlbumListResponse =
    AlbumListResponse(
        albums = this.map { it.toAlbumSummaryResponse() }
    )

/* 앨범 상세 조회 */
/* 앨범 생성 */
/* 앨범 삭제 */
fun Track.toResponse(): TrackResponse =
    TrackResponse(
        trackId = this.id,
        name = this.name,
        url = this.url.toString()
    )

fun Album.toAlbumDetailResponse(): AlbumDetailResponse =
    AlbumDetailResponse(
        albumId = this.id,
        name = this.name,
        coverImageUrl = this.coverImageUrl.toString(),
        releaseDate = this.releaseDate.toString(),
        description = this.description,
        tracks = this.tracks.map { it.toResponse() }
    )

/**
 *   Web Request → Domain Command Model
 */

/* 앨범 생성 */
fun TrackRequest.toCommand(): CreateTrackCommand =
    CreateTrackCommand(
        name = this.name,
        url = URI(this.url)
    )

fun AlbumCreateRequest.toCommand(): CreateAlbumCommand =
    CreateAlbumCommand(
        name = this.name,
        coverImageUrl = this.coverImageUrl?.let { URI(it) },
        releaseDate = OffsetDateTime.parse(this.releaseDate),
        description = this.description,
        tracks = this.tracks.map { it.toCommand() }
    )