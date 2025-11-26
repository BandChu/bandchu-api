package com.bandchu.api.domain.artist.dto

import com.bandchu.api.domain.artist.dto.response.ArtistListResponse
import com.bandchu.api.domain.artist.dto.response.ArtistSearchResponse
import com.bandchu.api.domain.artist.model.ArtiProfile
import com.bandchu.api.domain.concert.model.Concert

/* 아티스트 전체 목록 조회 */
fun ArtiProfile.toArtistListItemDto(): ArtistListItemDto =
    ArtistListItemDto(
        artistId = id,
        name = artistName,
        profileImageUrl = profileImageUrl.toString(),
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
        profileImageUrl = profileImageUrl.toString()
    )

fun Concert.toConcertSearchResultDto(): ConcertSearchResultDto =
    ConcertSearchResultDto(
        concertId = id,
        title = title,
        place = place,
        posterImageUrl = posterImageUrl.toString()
    )

fun Pair<List<ArtiProfile>, List<Concert>>.toSearchResponse(): ArtistSearchResponse {
    val (artists, concerts) = this

    return ArtistSearchResponse(
        artists = artists.map { it.toArtistSearchResultDto() },
        concerts = concerts.map { it.toConcertSearchResultDto() }
    )
}