package com.bandchu.api.domain.artists.controller

import com.bandchu.api.domain.artist.dto.request.ArtistUpdateRequest
import com.bandchu.api.domain.artist.model.ArtiProfile
import com.bandchu.api.domain.artists.ArtisTestConfig
import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.fixture.ArtistFixture
import com.bandchu.api.fixture.AuthFixture
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional

/**
 * Artist API 통합 테스트
 *
 * - MockMvc 기반으로 Controller–Service–Repository 흐름을 검증합니다.
 * - 실제 JWT 검증은 수행하지 않으며,
 *   SecurityMockMvcRequestPostProcessors.user()를 사용해
 *   SecurityContext에 인증 정보를 직접 주입하여 인증/인가를 테스트합니다.
 */

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Import(ArtisTestConfig::class)
class ArtistApiTest (
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val authFixture: AuthFixture,
    private val artistFixture: ArtistFixture
) : DescribeSpec() {

    private lateinit var member1: Member
    private lateinit var member2: Member
    private lateinit var artiProfile1: ArtiProfile
    private lateinit var artiProfile2: ArtiProfile

    init {
        beforeSpec {
            // 현재 테스트로 생성된 데이터 롤백 x, DB 자동 롤백 방법 생각해보기
            val suffix = System.currentTimeMillis()

            val u1 = authFixture.createMember(
                AuthFixture.AuthCredentials(
                    "member1-$suffix@test.com",
                    "pass1111",
                    "아티스트 1",
                    Role.ARTIST
                )
            )
            val u2 = authFixture.createMember(
                AuthFixture.AuthCredentials(
                    "member2-$suffix@test.com",
                    "pass2222",
                    "아티스트 2",
                    Role.ARTIST
                )
            )

            member1 = u1
            member2 = u2

            authFixture.authenticateAs(u1)
            artiProfile1 = artistFixture.createArtiProfile(u1)
            authFixture.authenticateAs(u2)
            artiProfile2 = artistFixture.createArtiProfile(u2)
        }

        describe("전체 아티스트 목록 조회") {
            context("유효한 요청인 경우") {
                it("성공(200)과 지정한 응답 포맷을 반환한다") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/artists")
                            .with(user(member1.id.toString()).roles(member1.role.name))
                    ).andReturn().response

                    result.status shouldBe 200

                    val root = objectMapper.readTree(result.contentAsString)
                    root["data"]["artists"].isArray shouldBe true
                }
            }
        }

        describe("아티스트 및 공연 검색") {
            context("검색 키워드를 포함한 유효한 요청인 경우") {
                it("성공(200)과 지정한 응답 포맷을 반환한다") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/artists/search")
                            .param("keyword", "리도어")
                            .with(user(member1.id.toString()).roles(member1.role.name))
                    ).andReturn().response

                    result.status shouldBe 200

                    val root = objectMapper.readTree(result.contentAsString)
                    val dataNode = root["data"]
                    dataNode.has("artists") shouldBe true
                    dataNode["artists"].isArray shouldBe true
                    dataNode.has("concerts") shouldBe true
                    dataNode["concerts"].isArray shouldBe true
                }
            }
        }

        describe("아티 프로필 상세 조회") {
            context("존재하는 아티 프로필에 대한 요청인 경우") {
                it("성공(200)과 지정한 응답 포맷을 반환한다") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/artists/${artiProfile1.id}")
                            .with(user(member1.id.toString()).roles(member1.role.name))
                    ).andReturn().response

                    result.status shouldBe 200

                    val root = objectMapper.readTree(result.contentAsString)
                    val dataNode = root["data"]
                    dataNode["artistId"].asLong() shouldBe artiProfile1.id
                    dataNode["name"].asText() shouldBe "아티스트 1"
                }
            }

            context("존재하지 않는 아티 프로필에 대한 요청인 경우") {
                it("요청한 리소스를 찾을 수 없음(404)와 지정한 에러 포맷을 반환한다") {
                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/artists/99999")
                            .with(user(member1.id.toString()).roles(member1.role.name))
                    ).andReturn().response

                    result.status shouldBe 404

                    val root = objectMapper.readTree(result.contentAsString)
                    root["title"].asText() shouldBe "Not Found"
                    root["detail"].asText() shouldBe "요청한 아티 프로필을 찾을 수 없습니다."
                    root["code"].asText() shouldBe "ARTIST_NOT_FOUND"
                }
            }
        }

        describe("아티 프로필 수정") {
            context("접속한 사용자의 아티 프로필이며, 유효한 요청일 경우") {
                it("성공(200)과 지정한 응답 포맷을 반환한다") {
                    val request = ArtistUpdateRequest(
                        name = "아티스트 1-수정",
                        profileImageUrl = null,
                        description = "수정된 소개",
                        genre = emptyList(),
                        sns = emptyList()
                    )

                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/artists/${artiProfile1.id}")
                            .with(user(member1.id.toString()).roles(member1.role.name))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    ).andReturn().response

                    result.status shouldBe 200

                    val root = objectMapper.readTree(result.contentAsString)
                    val dataNode = root["data"]
                    dataNode["artistId"].asLong() shouldBe artiProfile1.id
                    dataNode["name"].asText() shouldBe "아티스트 1-수정"
                }
            }

            context("존재하지 않는 아티 프로필에 대한 요청인 경우") {
                it("요청한 리소스를 찾을 수 없음(404)와 지정한 에러 포맷을 반환한다") {
                    val request = ArtistUpdateRequest(
                        name = "없는 아티스트",
                        profileImageUrl = null,
                        description = null,
                        genre = emptyList(),
                        sns = emptyList()
                    )

                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/artists/99999")
                            .with(user(member1.id.toString()).roles(member1.role.name))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    ).andReturn().response

                    result.status shouldBe 404

                    val root = objectMapper.readTree(result.contentAsString)
                    root["title"].asText() shouldBe "Not Found"
                    root["detail"].asText() shouldBe "요청한 아티 프로필을 찾을 수 없습니다."
                    root["code"].asText() shouldBe "ARTIST_NOT_FOUND"
                }
            }

            context("접속한 사용자의 아티 프로필이 아닐 경우") {
                it("권한 없음(403)와 지정한 에러 포맷을 반환한다") {
                    val request = ArtistUpdateRequest(
                        name = "남의 아티스트 수정 시도",
                        profileImageUrl = null,
                        description = null,
                        genre = emptyList(),
                        sns = emptyList()
                    )

                    val result = mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/artists/${artiProfile2.id}")
                            .with(user(member1.id.toString()).roles(member1.role.name))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    ).andReturn().response

                    result.status shouldBe 403

                    val root = objectMapper.readTree(result.contentAsString)
                    root["title"].asText() shouldBe "Forbidden"
                    root["detail"].asText() shouldBe "해당 아티 프로필에 대한 접근 권한이 없습니다."
                    root["code"].asText() shouldBe "ARTIST_FORBIDDEN"
                }
            }
        }
    }
}