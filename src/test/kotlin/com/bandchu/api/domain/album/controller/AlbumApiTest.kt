package com.bandchu.api.domain.album.controller

import com.bandchu.api.domain.album.AlbumTestConfig
import com.bandchu.api.domain.album.model.Album
import com.bandchu.api.domain.album.service.dto.CreateAlbumCommand
import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.domain.member.service.GoogleOAuthService
import com.bandchu.api.fixture.AlbumFixture
import com.bandchu.api.fixture.ArtistFixture
import com.bandchu.api.fixture.AuthFixture
import com.bandchu.api.global.config.ConfigController
import com.bandchu.api.global.config.S3Uploader
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.time.OffsetDateTime

/**
 * Album API 통합 테스트
 *
 * - MockMvc 기반으로 Controller–Service–Repository 흐름을 검증합니다.
 * - 실제 JWT 검증은 수행하지 않으며,
 *   SecurityMockMvcRequestPostProcessors.user()를 사용해
 *   SecurityContext에 인증 정보를 직접 주입하여 인증/인가를 테스트합니다.
 */

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Import(AlbumTestConfig::class)
class AlbumApiTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val authFixture: AuthFixture,
    private val artistFixture: ArtistFixture,
    private val albumFixture: AlbumFixture,
) : DescribeSpec() {

    private lateinit var artistMember: Member
    private lateinit var fanMember: Member
    private lateinit var otherArtistMember: Member
    private lateinit var noArtiProfileMember: Member
    private lateinit var myAlbum: Album // artistMember가 생성할 앨범
    private lateinit var otherAlbum: Album // otherArtistMember가 생성할 앨범

    private val NON_EXISTENT_ID = 99999L
    private val ARTIST_ROLE = Role.ARTIST.name
    private val FAN_ROLE = Role.FAN.name
    @MockkBean(relaxed = true)
    lateinit var s3Uploader: S3Uploader

    @MockkBean(relaxed = true)
    lateinit var googleOAuthService: GoogleOAuthService

    @MockkBean(relaxed = true)
    lateinit var configController: ConfigController
    init {
        beforeSpec {
            val suffix = System.currentTimeMillis()

            artistMember = authFixture.createMember(AuthFixture.AuthCredentials("artist-$suffix@test.com", "pass", "ArtistUser", Role.ARTIST))
            fanMember = authFixture.createMember(AuthFixture.AuthCredentials("fan-$suffix@test.com", "pass", "NormalUser", Role.FAN))
            otherArtistMember = authFixture.createMember(AuthFixture.AuthCredentials("other-$suffix@test.com", "pass", "OtherArtist", Role.ARTIST))
            noArtiProfileMember = authFixture.createMember(AuthFixture.AuthCredentials("no-$suffix@test.com", "pass", "noArtiProfileArtist", Role.ARTIST))

            authFixture.authenticateAs(artistMember)
            val myArtiProfile = artistFixture.createArtiProfile(artistMember)
            myAlbum = albumFixture.createAlbum("내 앨범", myArtiProfile)

            authFixture.authenticateAs(otherArtistMember)
            val otherArtiProfile = artistFixture.createArtiProfile(otherArtistMember)
            otherAlbum = albumFixture.createAlbum("다른 앨범", otherArtiProfile)
        }

        describe("앨범 상세 조회") {
            context("팬 또는 아티스트 역할의 사용자가 존재하는 리소스에 대해 요청한 경우") {
                it("성공(200)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/albums/${myAlbum.id}")
                            .with(user(fanMember.id.toString()).roles(FAN_ROLE)) // 팬 권한 주입
                    ).andReturn().response

                    result.status shouldBe HttpStatus.OK.value()
                    val root = objectMapper.readTree(result.contentAsString)

                    root["success"].asBoolean() shouldBe true
                    root["data"]["albumId"].asLong() shouldBe myAlbum.id
                    root["data"]["name"].asText() shouldBe myAlbum.name
                }
            }

            context("요청한 앨범이 존재하지 않는 경우") {
                it("요청한 리소스를 찾을 수 없음(404)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/albums/$NON_EXISTENT_ID")
                            .with(user(artistMember.id.toString()).roles(ARTIST_ROLE)) // 유효한 사용자 Context
                    ).andReturn().response

                    result.status shouldBe HttpStatus.NOT_FOUND.value()
                    val root = objectMapper.readTree(result.contentAsString)
                    root["title"].asText() shouldBe "Not Found"
                }
            }
        }

        describe("앨범 등록") {
            val validRequest = CreateAlbumCommand(
                    "새로운 앨범",
                URI("banchu.test.com"),
                OffsetDateTime.now(),
                null,
                emptyList()
                    )
            val requestJson = objectMapper.writeValueAsString(validRequest)

            context("아티스트 역할의 사용자가 올바른 형식으로 요청한 경우") {
                it("성공(201)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/albums")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson)
                            .with(user(artistMember.id.toString()).roles(ARTIST_ROLE))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.CREATED.value()
                    val root = objectMapper.readTree(result.contentAsString)
                    root["data"]["name"].asText() shouldBe "새로운 앨범"
                }
            }

            context("현재 접속한 사용자 역할이 아티스트가 아닌 경우") {
                it("권한 없음(403)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/albums")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson)
                            .with(user(fanMember.id.toString()).roles(FAN_ROLE))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.FORBIDDEN.value()
                }
            }

            context("아티스트 역할의 사용자이지만, 아직 아티 프로필을 생성하지 않은 경우") {
                it("리소스 충돌 발생(409)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/albums")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson)
                            .with(user(noArtiProfileMember.id.toString()).roles(ARTIST_ROLE))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.CONFLICT.value()
                }
            }
        }

        describe("앨범 삭제") {
            context("아티스트 역할의 사용자가 올바른 형식으로 요청한 경우") {
                it("성공(204)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/albums/${myAlbum.id}")
                            .with(user(artistMember.id.toString()).roles(ARTIST_ROLE))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.NO_CONTENT.value()
                }
            }

            context("요청한 앨범이 존재하지 않는 경우") {
                it("요청한 리소스를 찾을 수 없음(404)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/albums/$NON_EXISTENT_ID")
                            .with(user(artistMember.id.toString()).roles(ARTIST_ROLE))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.NOT_FOUND.value()
                }
            }

            context("접속한 사용자의 아티 프로필이 아닐 경우") {
                it("권한 없음(403)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/albums/${otherAlbum.id}")
                            .with(user(artistMember.id.toString()).roles(ARTIST_ROLE))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.FORBIDDEN.value()
                }
            }
        }
    }
}