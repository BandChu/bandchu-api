package com.bandchu.api.domain.album.service

import com.bandchu.api.domain.album.model.Album
import com.bandchu.api.domain.album.repository.AlbumRepository
import com.bandchu.api.domain.album.service.dto.CreateAlbumCommand
import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import com.bandchu.api.global.util.getCurrentUserId
import com.bandchu.api.global.util.getCurrentUserRole
import org.springframework.stereotype.Service

@Service
class AlbumService(
    private val albumRepository: AlbumRepository
) {
    fun getDetail(albumId: Long): Album {
        return albumRepository.getDetail(albumId)
    }

    fun create(command: CreateAlbumCommand): Album {
        if (getCurrentUserRole() != Role.ARTIST) throw BusinessException(ErrorCode.SUBSCRIPTION_INSUFFICIENT_ROLE)

        return albumRepository.createProcess(command, getCurrentUserId())
    }

    fun delete(albumId: Long): Unit {
        if (getCurrentUserRole() != Role.ARTIST) throw BusinessException(ErrorCode.SUBSCRIPTION_INSUFFICIENT_ROLE)

        return albumRepository.delete(albumId, getCurrentUserId())
    }
}