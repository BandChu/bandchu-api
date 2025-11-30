package com.bandchu.api.fixture

import com.bandchu.api.domain.artist.model.ArtiProfile
import com.bandchu.api.domain.concert.model.Concert
import com.bandchu.api.domain.concert.service.ConcertService
import com.bandchu.api.domain.concert.service.dto.CreateConcertCommand
import java.net.URI

class ConcertFixture(
    private val concertService: ConcertService
) {
    /**
     * 외부 환경에 대한 의존을 줄이고, 재사용 가능한 코드를 만들기 위한 테스트 객체 생성 Fixture
     * 특정 Member의 Arti-Profile을 주입해 Concert를 생성하고, Concert 객체를 반환
     */
    fun createConcert(title: String, place: String, artiProfile: ArtiProfile): Concert {
        val concert = CreateConcertCommand(
            title = title,
            place = place,
            posterImageUrl = URI("test.bandchu.com"),
            information = "Test Concert for artist ${artiProfile.id}",
            bookingSchedule = null,
            bookingUrl = URI("test.bandchu.com"),
        )

        return concertService.create(concert)
    }
}