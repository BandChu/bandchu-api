package com.bandchu.api.domain.artist.service

import com.bandchu.api.domain.artist.model.ArtiProfile
import com.bandchu.api.domain.artist.repository.ArtiProfileRepository
import com.bandchu.api.domain.artist.service.dto.CreateArtistDetailCommand
import com.bandchu.api.domain.artist.service.dto.UpdateArtistDetailCommand
import com.bandchu.api.domain.concert.model.Concert
import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import com.bandchu.api.global.util.getCurrentUserId
import com.bandchu.api.global.util.getCurrentUserRole
import org.springframework.stereotype.Service

@Service
class ArtistService(
    private val artistRepository: ArtiProfileRepository,

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
}