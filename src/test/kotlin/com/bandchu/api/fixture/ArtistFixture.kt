package com.bandchu.api.fixture

import com.bandchu.api.domain.artist.model.ArtiProfile
import com.bandchu.api.domain.artist.model.Genre
import com.bandchu.api.domain.artist.service.ArtistService
import com.bandchu.api.domain.artist.service.dto.CreateArtistDetailCommand
import com.bandchu.api.domain.member.model.Member
import java.net.URI

class ArtistFixture(
    private val artistService: ArtistService
) {
    /**
     * 외부 환경에 대한 의존을 줄이고, 재사용 가능한 코드를 만들기 위한 테스트 객체 생성 Fixture입니다.
     * 특정 Member를 위한 Artist Profile을 생성하고, 생성된 ArtistProfile 객체를 반환합니다.
     */
    fun createArtiProfile(member: Member): ArtiProfile {
        val artiProfile = CreateArtistDetailCommand(
            name = member.nickname,
            profileImageUrl = URI("test.bandchu.com"),
            description = "Test profile for member ${member.id}",
            genre = emptyList(),
            sns = emptyList()
        )

        return artistService.createDetail(artiProfile)
    }
}