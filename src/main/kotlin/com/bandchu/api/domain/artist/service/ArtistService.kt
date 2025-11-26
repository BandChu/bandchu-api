package com.bandchu.api.domain.artist.service

import com.bandchu.api.domain.artist.model.ArtiProfile
import com.bandchu.api.domain.artist.repository.ArtiProfileRepository
import com.bandchu.api.domain.artist.repository.SnsLinkRepository
import com.bandchu.api.domain.artist.service.dto.UpdateArtistDetailCommand
import com.bandchu.api.domain.concert.model.Concert
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArtistService(
    private val artistRepository: ArtiProfileRepository,
    private val snsLinkRepository: SnsLinkRepository
) {
    fun getAll(): List<ArtiProfile> {
        // TODO: 인증인가 구현 후 접근 제어 추가
        return artistRepository.findAll()
    }

    fun search(searchCondition: String): Pair<List<ArtiProfile>, List<Concert>> {
        // TODO: 인증인가 구현 후 접근 제어 추가
        return artistRepository.searchArtistsAndConcerts(searchCondition);
    }

    fun getDetail(id: Long): ArtiProfile {
        // TODO: 인증인가 구현 후 접근 제어 추가

        return artistRepository.findById(id)
            ?: throw BusinessException(ErrorCode.ARTIST_NOT_FOUND)
    }

    @Transactional
    fun updateDetail(command: UpdateArtistDetailCommand): ArtiProfile {
        // TODO: 인증인가 구현 후 접근 제어 추가

        val artist = artistRepository.findById(command.artistId)
            ?: throw BusinessException(ErrorCode.ARTIST_NOT_FOUND)

        // if (artist.id != 현재 접속한 멤버의 id) throw BusinessException(ErrorCode.ARTIST_FORBIDDEN)

        artistRepository.updateArtist(command)
        snsLinkRepository.replaceAll(command.artistId, command.sns)

        return artistRepository.findById(command.artistId)!!
    }
}