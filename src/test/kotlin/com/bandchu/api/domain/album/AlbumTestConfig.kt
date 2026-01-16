package com.bandchu.api.domain.album

import com.bandchu.api.domain.album.service.AlbumService
import com.bandchu.api.domain.artist.service.ArtistService
import com.bandchu.api.domain.member.repository.MemberRepository
import com.bandchu.api.domain.member.service.MemberService
import com.bandchu.api.fixture.AlbumFixture
import com.bandchu.api.fixture.ArtistFixture
import com.bandchu.api.fixture.AuthFixture
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class AlbumTestConfig {
    @Bean
    fun authFixture(
        memberservice: MemberService,
    ): AuthFixture {
        return AuthFixture(
            memberService = memberservice,
        )
    }

    @Bean
    fun artistFixture(
        artistService: ArtistService
    ): ArtistFixture {
        return ArtistFixture(
            artistService = artistService
        )
    }

    @Bean
    fun albumFixture(
        albumService: AlbumService
    ): AlbumFixture {
        return AlbumFixture(
            albumService = albumService
        )
    }
}