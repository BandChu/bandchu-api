package com.bandchu.api.domain.artist.service

import com.bandchu.api.domain.album.dto.toAlbumSummaryResponse
import com.bandchu.api.domain.album.repository.AlbumRepository
import com.bandchu.api.domain.artist.dto.response.ArtistMeResponse
import com.bandchu.api.domain.artist.dto.toArtistDetailResponse
import com.bandchu.api.domain.artist.model.ArtiProfile
import com.bandchu.api.domain.artist.repository.ArtiProfileRepository
import com.bandchu.api.domain.artist.service.dto.CreateArtistDetailCommand
import com.bandchu.api.domain.artist.service.dto.UpdateArtistDetailCommand
import com.bandchu.api.domain.concert.dto.toConcertDetailResponse
import com.bandchu.api.domain.concert.model.Concert
import com.bandchu.api.domain.concert.repository.ConcertRepository
import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import com.bandchu.api.global.util.getCurrentUserId
import com.bandchu.api.global.util.getCurrentUserRole
import org.springframework.stereotype.Service

@Service
class ArtistService(
    private val artistRepository: ArtiProfileRepository,
    private val albumRepository: AlbumRepository,
    private val concertRepository: ConcertRepository

    ) {
    fun getAll(): List<ArtiProfile> {
        return artistRepository.findAll()
    }

    fun search(searchCondition: String): Pair<List<ArtiProfile>, List<Concert>> {
        return artistRepository.searchArtistsAndConcerts(searchCondition);
    }

    fun getDetail(id: Long): ArtiProfile {
        return artistRepository.findById(id)
            ?: throw BusinessException(ErrorCode.ARTIST_NOT_FOUND)
    }

    fun createDetail(command: CreateArtistDetailCommand): ArtiProfile {
        if (getCurrentUserRole() != Role.ARTIST) throw BusinessException(ErrorCode.ARTIST_INSUFFICIENT_ROLE)

        return artistRepository.createProcess(command, getCurrentUserId())
            ?: throw BusinessException(ErrorCode.ARTIST_NOT_FOUND)
    }

    fun updateDetail(command: UpdateArtistDetailCommand): ArtiProfile {
        if (getCurrentUserRole() != Role.ARTIST) throw BusinessException(ErrorCode.ARTIST_INSUFFICIENT_ROLE)

        val artist = artistRepository.findById(command.artistId)
            ?: throw BusinessException(ErrorCode.ARTIST_NOT_FOUND)

        if (getCurrentUserId() !=  artist.memberId) throw BusinessException(ErrorCode.ARTIST_FORBIDDEN)

        return artistRepository.updateProcess(command)
            ?: throw BusinessException(ErrorCode.ARTIST_NOT_FOUND)
    }

    fun getMyDetail(currentUserId: Long): ArtistMeResponse {
        // 역할 체크를 try-catch로 감싸서 안전하게 처리
        try {
            val currentRole = getCurrentUserRole()
            if (currentRole != Role.ARTIST) {
                throw BusinessException(ErrorCode.ARTIST_INSUFFICIENT_ROLE)
            }
        } catch (e: BusinessException) {
            // 이미 BusinessException이면 그대로 던짐
            throw e
        } catch (e: Exception) {
            // 다른 예외(예: USER_INVALID_CREDENTIAL)는 그대로 던짐
            throw BusinessException(ErrorCode.USER_INVALID_CREDENTIAL)
        }

        val artiProfile: ArtiProfile = artistRepository.findByMemberId(currentUserId) ?: return ArtistMeResponse(
            isExists = false,
            artist = null
        )

        val artiProfileId = artiProfile.id
        val albums = albumRepository.getAllSummaryByArtist(artiProfileId)
        val concerts = concertRepository.getAllByArtist(artiProfileId)

        return ArtistMeResponse(
            isExists = true,
            artist = artiProfile.toArtistDetailResponse(),
            albums = albums.map { it.toAlbumSummaryResponse() },
            concerts = concerts.map { it.toConcertDetailResponse() }
        )
    }
}