package com.bandchu.api.domain.concert.controller

import com.bandchu.api.domain.artist.service.ArtistService
import com.bandchu.api.domain.concert.model.Concert
import com.bandchu.api.domain.concert.service.ConcertService
import com.bandchu.api.domain.concert.service.dto.CreateConcertCommand
import com.bandchu.api.domain.concert.service.dto.UpdateConcertCommand
import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.domain.member.service.MemberService
import com.bandchu.api.fixture.ArtistFixture
import com.bandchu.api.fixture.AuthFixture
import com.bandchu.api.fixture.ConcertFixture
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional
import java.net.URI

@TestConfiguration
class ConcertApiTestConfig {
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

    @Bean
    fun concertFixture(
        concertService: ConcertService
    ): ConcertFixture {
        return ConcertFixture(
            concertService = concertService
        )
    }
}

/**
 * Concert API 통합 테스트
 *
 * - MockMvc 기반으로 Controller–Service–Repository 흐름을 검증합니다.
 * - 실제 JWT 검증은 수행하지 않으며,
 *   SecurityMockMvcRequestPostProcessors.user()를 사용해
 *   SecurityContext에 인증 정보를 직접 주입하여 인증/인가를 테스트합니다.
 */

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Import(ConcertApiTestConfig::class)
class ConcertApiTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val authFixture: AuthFixture,
    private val artistFixture: ArtistFixture,
    private val concertFixture: ConcertFixture
) : DescribeSpec() {
    private lateinit var artistMember: Member       // 공연 생성 권한이 있는 사용자 (Role.ARTIST)
    private lateinit var fanMember: Member       // 구독 조회 권한이 있는 일반 사용자 (Role.FAN)
    private lateinit var otherArtistMember: Member  // 다른 아티스트 (403 Forbidden 테스트용)
    private lateinit var myConcert: Concert       // artistMember가 생성한 기본 콘서트
    private lateinit var otherConcert: Concert

    private val NON_EXISTENT_ID = 99999L
    private val ARTIST_ROLE = Role.ARTIST.name
    private val FAN_ROLE = Role.FAN.name

    init {
        beforeSpec {
            val suffix = System.currentTimeMillis()

            artistMember = authFixture.createMember(AuthFixture.AuthCredentials("artist-$suffix@test.com", "pass", "ArtistUser", Role.ARTIST))
            fanMember = authFixture.createMember(AuthFixture.AuthCredentials("user-$suffix@test.com", "pass", "NormalUser", Role.FAN))
            otherArtistMember = authFixture.createMember(AuthFixture.AuthCredentials("other-$suffix@test.com", "pass", "OtherArtist", Role.ARTIST))

            authFixture.authenticateAs(artistMember)
            val myArtiProfile = artistFixture.createArtiProfile(artistMember)
            myConcert = concertFixture.createConcert("내 콘서트 제목", "내 콘서트 장소", myArtiProfile)

            authFixture.authenticateAs(otherArtistMember)
            val otherArtiProfile = artistFixture.createArtiProfile(otherArtistMember)
            otherConcert = concertFixture.createConcert("다른 콘서트 제목", "다른 콘서트 장소", otherArtiProfile)
        }

        describe("공연 상세 조회") {
            context("팬 또는 아티스트 역할의 사용자가 존재하는 리소스에 대해 요청한 경우") {
                it("성공(200)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/concerts/${myConcert.id}")
                            .with(user(artistMember.id.toString()).roles(ARTIST_ROLE))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.OK.value()
                    val root = objectMapper.readTree(result.contentAsString)
                    root["data"]["title"].asText() shouldBe myConcert.title
                }
            }

            context("요청한 공연이 존재하지 않는 경우") {
                it("요청한 리소스를 찾을 수 없음(404)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/concerts/$NON_EXISTENT_ID")
                            .with(user(artistMember.id.toString()).roles(ARTIST_ROLE))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.NOT_FOUND.value()
                }
            }
        }

        describe("구독한 아티스트의 공연 조회") {
            context("팬 역할의 사용자가 올바른 형식으로 요청한 경우") {
                it("성공(200)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/concerts/subscribed")
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(user(fanMember.id.toString()).roles(FAN_ROLE))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.OK.value()
                    val root = objectMapper.readTree(result.contentAsString)

                    val artistsNode = root["data"]["artists"]
                    artistsNode.isArray shouldBe true

                    val artistIds = artistsNode.map { it["artistId"].asLong() }
                    artistIds.all { it > 0L } shouldBe true

                    // TODO: 구독 정보 추가해서 확인하면 좋을 듯
                }
            }
            context("현재 접속한 사용자 역할이 팬이 아닌 경우") {
                it("권한 없음(403)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/concerts/subscribed")
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(user(artistMember.id.toString()).roles(ARTIST_ROLE))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.FORBIDDEN.value()
                }
            }
        }

        describe("공연 등록") {
            val validRequest = CreateConcertCommand(
                "새로운 공연",
                "새로운 장소",
                URI("banchu.test.com"),
                "공연 정보",
                null,
                null
            )
            val requestJson = objectMapper.writeValueAsString(validRequest)

            context("아티스트 역할의 사용자가 올바른 형식으로 요청한 경우") {
                it("성공(201)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/concerts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson)
                            .with(user(artistMember.id.toString()).roles(ARTIST_ROLE))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.CREATED.value()
                    val root = objectMapper.readTree(result.contentAsString)
                    root["data"]["title"].asText() shouldBe "새로운 공연"
                }
            }

            context("현재 접속한 사용자 역할이 아티스트가 아닌 경우") {
                it("권한 없음(403)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/concerts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson)
                            .with(user(fanMember.id.toString()).roles(FAN_ROLE))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.FORBIDDEN.value()
                }
            }
        }

        describe("공연 수정") {
            val validRequest = UpdateConcertCommand(
                myConcert.id,
                "수정된 공연",
                "수정된 장소",
                URI("banchu.test.com"),
                "공연 정보",
                null,
                null,
                emptyList()
            )
            val updateJson = objectMapper.writeValueAsString(validRequest)

            context("접속한 사용자의 아티 프로필이며, 유효한 요청일 경우") {
                it("성공(200)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/concerts/${myConcert.id}")
                            .with(user(artistMember.id.toString()).roles(ARTIST_ROLE))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson)
                    ).andReturn().response

                    result.status shouldBe HttpStatus.OK.value()
                    val root = objectMapper.readTree(result.contentAsString)
                    root["data"]["title"].asText() shouldBe "수정된 공연"
                }
            }

            context("접속한 사용자의 아티 프로필이 아닐 경우") {
                it("권한 없음(403)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/concerts/${otherConcert.id}")
                            .with(user(artistMember.id.toString()).roles(ARTIST_ROLE))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson)
                    ).andReturn().response

                    result.status shouldBe HttpStatus.FORBIDDEN.value()
                }
            }
        }

        describe("공연 삭제") {
            context("접속한 사용자의 아티 프로필이며, 유효한 요청일 경우") {
                it("성공(200)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/concerts/${myConcert.id}")
                            .with(user(artistMember.id.toString()).roles(ARTIST_ROLE))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.NO_CONTENT.value()
                }
            }

            context("요청한 공연이 존재하지 않는 경우") {
                it("요청한 리소스를 찾을 수 없음(404)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/concerts/$NON_EXISTENT_ID")
                            .with(user(artistMember.id.toString()).roles(ARTIST_ROLE))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.NOT_FOUND.value()
                }
            }

            context("접속한 사용자의 아티 프로필이 아닐 경우") {
                it("권한 없음(403)") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/concerts/${otherConcert.id}")
                            .with(user(artistMember.id.toString()).roles(ARTIST_ROLE))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.FORBIDDEN.value()
                }
            }
        }
    }
}