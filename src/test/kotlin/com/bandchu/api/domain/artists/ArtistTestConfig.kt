package com.bandchu.api.domain.artists

import com.bandchu.api.domain.artist.service.ArtistService
import com.bandchu.api.domain.member.service.MemberService
import com.bandchu.api.fixture.ArtistFixture
import com.bandchu.api.fixture.AuthFixture
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class ArtisTestConfig {
    @Bean
    fun authFixture(
        memberService: MemberService
    ): AuthFixture {
        return AuthFixture(
            memberService = memberService
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
}