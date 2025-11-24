package com.bandchu.api.domain.artist.service

import com.bandchu.api.domain.artist.model.ArtiProfile
import com.bandchu.api.domain.artist.repository.ArtiProfileRepository
import org.springframework.stereotype.Service

@Service
class ArtistService(
    private val artistRepository: ArtiProfileRepository
) {
    fun getAll(): List<ArtiProfile> {
        // TODO: 인증인가 구현 후 접근 제어 추가
        return artistRepository.findAll()
    }
}