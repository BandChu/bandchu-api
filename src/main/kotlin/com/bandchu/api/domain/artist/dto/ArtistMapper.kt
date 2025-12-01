package com.bandchu.api.domain.artist.dto

import com.bandchu.api.domain.artist.dto.request.ArtistCreateRequest
import com.bandchu.api.domain.artist.dto.request.ArtistUpdateRequest
import com.bandchu.api.domain.artist.dto.response.ArtistDetailResponse
import com.bandchu.api.domain.artist.dto.response.ArtistListResponse
import com.bandchu.api.domain.artist.dto.response.ArtistSearchResponse
import com.bandchu.api.domain.artist.model.ArtiProfile
import com.bandchu.api.domain.artist.model.SnsLink
import com.bandchu.api.domain.artist.service.dto.ArtistSnsCommand
import com.bandchu.api.domain.artist.service.dto.CreateArtistDetailCommand
import com.bandchu.api.domain.artist.service.dto.UpdateArtistDetailCommand
import com.bandchu.api.domain.concert.model.Concert
import java.net.URI

/**
 *  Domain Model → Web Response
 */

/* 아티스트 전체 목록 조회 */
fun ArtiProfile.toArtistListItemDto(): ArtistListItemDto =
    ArtistListItemDto(
        artistId = id,
        name = artistName,
        profileImageUrl = profileImageUrl?.toString(),
        createdAt = createdAt.toString(),
    )

fun List<ArtiProfile>.toArtistListResponse(): ArtistListResponse =
    ArtistListResponse(
        artists = this.map { it.toArtistListItemDto() },
    )

/* 아티스트 및 공연 검색 */
fun ArtiProfile.toArtistSearchResultDto(): ArtistSearchResultDto =
    ArtistSearchResultDto(
        artistId = id,
        name = artistName,
        profileImageUrl = profileImageUrl?.toString()
    )

fun Concert.toConcertSearchResultDto(): ConcertSearchResultDto =
    ConcertSearchResultDto(
        concertId = id,
        title = title,
        place = place,
        posterImageUrl = posterImageUrl?.toString()
    )

fun Pair<List<ArtiProfile>, List<Concert>>.toSearchResponse(): ArtistSearchResponse {
    val (artists, concerts) = this

    return ArtistSearchResponse(
        artists = artists.map { it.toArtistSearchResultDto() },
        concerts = concerts.map { it.toConcertSearchResultDto() }
    )
}

/* 아티 프로필 상세 조회 */
/* 아티 프로필 생성 */
/* 아티 프로필 수정 */
fun SnsLink.toArtistSnsDto(): ArtistSnsDto =
    ArtistSnsDto(
        platform = platform,
        url = url.toString()
    )

fun ArtiProfile.toArtistDetailResponse(): ArtistDetailResponse {
    return ArtistDetailResponse(
        artistId = id,
        name = artistName,
        profileImageUrl = profileImageUrl?.toString(),
        description = description,
        genre = genre.map { it.name },
        sns = snsLinks.map { it.toArtistSnsDto() }
    )
}

/**
 *   Web Request → Domain Command Model
 */

/* 아티 프로필 생성 */
fun ArtistCreateRequest.toCommand(): CreateArtistDetailCommand =
    CreateArtistDetailCommand(
        name = name,
        profileImageUrl = profileImageUrl?.let { URI(it) },
        description = description,
        genre = genre,
        sns = sns.map {
            ArtistSnsCommand(
                platform = it.platform,
                url = it.url
            )
        }
    )

/* 아티 프로필 수정 */
fun ArtistUpdateRequest.toCommand(artistId: Long): UpdateArtistDetailCommand =
    UpdateArtistDetailCommand(
        artistId = artistId,
        name = name,
        profileImageUrl = profileImageUrl?.let { URI(it) },
        description = description,
        genre = genre,
        sns = sns.map {
            ArtistSnsCommand(
                platform = it.platform,
                url = it.url
            )
        }
    )