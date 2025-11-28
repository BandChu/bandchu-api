package com.bandchu.api.domain.concert.service

import com.bandchu.api.domain.concert.ConcertRepository
import com.bandchu.api.domain.concert.model.Concert
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
    private val concertRepository: ConcertRepository
) {
    fun getDetail(concertId: Long): Concert {
        return concertRepository.getDetail(concertId)
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