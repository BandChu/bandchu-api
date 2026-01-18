package com.bandchu.api.domain.concert

import com.bandchu.api.domain.artist.service.ArtistService
import com.bandchu.api.domain.concert.service.ConcertService
import com.bandchu.api.domain.member.repository.MemberRepository
import com.bandchu.api.domain.member.service.MemberService
import com.bandchu.api.fixture.ArtistFixture
import com.bandchu.api.fixture.AuthFixture
import com.bandchu.api.fixture.ConcertFixture
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class ConcertTestConfig {
    @Bean
    fun authFixture(
        memberService: MemberService,
    ): AuthFixture {
        return AuthFixture(
            memberService = memberService,
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
    fun concertFixture(
        concertService: ConcertService
    ): ConcertFixture {
        return ConcertFixture(
            concertService = concertService
        )
    }
}