package com.bandchu.api.fixture

import com.bandchu.api.domain.album.model.Album
import com.bandchu.api.domain.album.service.AlbumService
import com.bandchu.api.domain.album.service.dto.CreateAlbumCommand
import com.bandchu.api.domain.artist.model.ArtiProfile
import java.net.URI
import java.time.OffsetDateTime

class AlbumFixture(
    private val albumService: AlbumService
) {
    /**
     * 외부 환경에 대한 의존을 줄이고, 재사용 가능한 코드를 만들기 위한 테스트 객체 생성 Fixture
     * 특정 Member의 Arti-Profile을 주입해 Album을 생성하고, Album을 객체를 반환
     */
    fun createAlbum(name: String, artiProfile: ArtiProfile): Album {
        val album = CreateAlbumCommand(
            name = name,
            coverImageUrl = URI("test.bandchu.com"),
            releaseDate = OffsetDateTime.now(),
            description = null,
            tracks = emptyList()
        )

        return albumService.create(album)
    }
}