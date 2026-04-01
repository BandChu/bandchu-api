package com.bandchu.api.domain.concert.service

import com.bandchu.api.domain.concert.repository.ConcertRepository
import com.bandchu.api.domain.concert.model.Concert
import com.bandchu.api.domain.concert.service.dto.ConcertSubscribedRead
import com.bandchu.api.domain.concert.service.dto.CreateConcertCommand
import com.bandchu.api.domain.concert.service.dto.UpdateConcertCommand
import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import com.bandchu.api.global.util.getCurrentUserId
import com.bandchu.api.global.util.getCurrentUserRole
import org.springframework.stereotype.Service

@Service
class ConcertService(
    private val concertRepository: ConcertRepository,
) {

    fun getNearbyConcerts(lat: Double, lng: Double): List<Concert> {
        return concertRepository.findNearbyConcerts(lat, lng)
    }

    fun getAllByArtist(artistId: Long): List<Concert> {
        return concertRepository.getAllByArtist(artistId)
    }

    fun getDetail(concertId: Long): Concert {
        val updated = concertRepository.incrementViewCount(concertId)
        if (updated == 0) {
            throw BusinessException(ErrorCode.CONCERT_NOT_FOUND)
        }
        return concertRepository.getDetail(concertId)
    }

    fun getSubscribed(): List<ConcertSubscribedRead> {
        if (getCurrentUserRole() != Role.FAN) throw BusinessException(ErrorCode.SUBSCRIPTION_INSUFFICIENT_ROLE)

        return concertRepository.getConcertsBySubscription(getCurrentUserId())
    }

    fun create(command: CreateConcertCommand): Concert {
        if (getCurrentUserRole() != Role.ARTIST) throw BusinessException(ErrorCode.ARTIST_INSUFFICIENT_ROLE)

        return concertRepository.createProcess(command, getCurrentUserId())
    }

    fun update(command: UpdateConcertCommand): Concert {
        if (getCurrentUserRole() != Role.ARTIST) throw BusinessException(ErrorCode.ARTIST_INSUFFICIENT_ROLE)

        return concertRepository.updateProcess(command, getCurrentUserId())
    }

    fun delete(concertId: Long) {
        if (getCurrentUserRole() != Role.ARTIST) throw BusinessException(ErrorCode.ARTIST_INSUFFICIENT_ROLE)

        return concertRepository.delete(concertId, getCurrentUserId())
    }
}