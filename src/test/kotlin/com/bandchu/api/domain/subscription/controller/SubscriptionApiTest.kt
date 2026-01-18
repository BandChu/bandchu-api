package com.bandchu.api.domain.subscription.controller

import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.domain.subscription.SubscriptionTestConfig
import com.bandchu.api.domain.subscription.dto.SubscriptionRequest
import com.bandchu.api.fixture.AuthFixture
import com.bandchu.api.fixture.SubscriptionFixture
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(SubscriptionTestConfig::class)
class SubscriptionApiTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val authFixture: AuthFixture,
    private val subscriptionFixture: SubscriptionFixture
) : DescribeSpec() {

    private lateinit var fan: Member
    private lateinit var fan2: Member
    private lateinit var artist: Member
    private var artiProfileId: Long = 0L
    private var artiProfileId2: Long = 0L
    private var artiProfileId3: Long = 0L

    init {
        beforeSpec {
            val suffix = System.currentTimeMillis()

            // FAN 회원 생성 (2명)
            fan = authFixture.createMember(
                AuthFixture.AuthCredentials(
                    "fan-$suffix@test.com",
                    "password123",
                    "FanUser",
                    Role.FAN
                )
            )

            fan2 = authFixture.createMember(
                AuthFixture.AuthCredentials(
                    "fan2-$suffix@test.com",
                    "password123",
                    "FanUser2",
                    Role.FAN
                )
            )

            // ARTIST 회원 생성
            artist = authFixture.createMember(
                AuthFixture.AuthCredentials(
                    "artist-$suffix@test.com",
                    "password123",
                    "ArtistUser",
                    Role.ARTIST
                )
            )

            // ArtiProfile 생성 (테스트용 3개)
            artiProfileId = subscriptionFixture.createArtiProfile(
                artistName = "NewJeans",
                artist = artist,
                genre = listOf("POP", "DANCE"),
                description = "대한민국의 5인조 걸그룹"
            )

            artiProfileId2 = subscriptionFixture.createArtiProfile(
                artistName = "IU",
                artist = artist,
                genre = listOf("BALLAD", "POP"),
                description = "대한민국의 솔로 가수"
            )

            artiProfileId3 = subscriptionFixture.createArtiProfile(
                artistName = "BTS",
                artist = artist,
                genre = listOf("HIPHOP", "POP"),
                description = "대한민국의 7인조 보이그룹"
            )
        }

        describe("구독 생성 및 조회 E2E 시나리오") {

            context("FAN이 아티스트 구독 후 목록 조회") {
                it("구독이 생성되고 목록에 포함됨") {
                    // 구독 생성
                    val subscribeRequest = SubscriptionRequest(artiProfileId = artiProfileId)

                    val subscribeResult = mockMvc.perform(
                        post("/api/subscriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(subscribeRequest))
                            .with(user(fan.id.toString()).roles("FAN"))
                    ).andReturn().response

                    subscribeResult.status shouldBe HttpStatus.CREATED.value()
                    val subscribeRoot = objectMapper.readTree(subscribeResult.contentAsString)
                    subscribeRoot["success"].asBoolean() shouldBe true
                    subscribeRoot["data"]["artiProfileId"].asLong() shouldBe artiProfileId
                    subscribeRoot["data"]["memberId"].asLong() shouldBe fan.id
                    subscribeRoot["message"].asText() shouldBe "아티스트를 구독했습니다."

                    // 구독 목록 조회
                    val listResult = mockMvc.perform(
                        get("/api/subscriptions")
                            .with(user(fan.id.toString()).roles("FAN"))
                    ).andReturn().response

                    listResult.status shouldBe HttpStatus.OK.value()
                    val listRoot = objectMapper.readTree(listResult.contentAsString)
                    listRoot["success"].asBoolean() shouldBe true
                    listRoot["data"].isArray shouldBe true
                    (listRoot["data"].size() >= 1) shouldBe true
                    listRoot["data"][0]["artiProfileId"].asLong() shouldBe artiProfileId
                    listRoot["data"][0]["artistName"].asText() shouldBe "NewJeans"
                }
            }

            context("이미 구독 중인 아티스트에 중복 구독 시도") {
                it("409 Conflict 반환") {
                    // 첫 번째 구독 생성
                    subscriptionFixture.subscribe(fan, artiProfileId2)

                    // 중복 구독 시도
                    val duplicateRequest = SubscriptionRequest(artiProfileId = artiProfileId2)

                    val result = mockMvc.perform(
                        post("/api/subscriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateRequest))
                            .with(user(fan.id.toString()).roles("FAN"))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.CONFLICT.value()
                    val root = objectMapper.readTree(result.contentAsString)
                    root["code"].asText() shouldBe "SUBSCRIPTION_DUPLICATED"
                    root["detail"].asText() shouldBe "이미 구독 중인 아티스트입니다."
                }
            }

            context("ARTIST 역할이 구독 시도") {
                it("403 Forbidden 반환") {
                    val request = SubscriptionRequest(artiProfileId = artiProfileId)

                    val result = mockMvc.perform(
                        post("/api/subscriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(artist.id.toString()).roles("ARTIST"))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.FORBIDDEN.value()
                    val root = objectMapper.readTree(result.contentAsString)
                    root["code"].asText() shouldBe "SUBSCRIPTION_INSUFFICIENT_ROLE"
                }
            }

            context("존재하지 않는 아티스트 구독 시도") {
                it("404 Not Found 반환") {
                    val invalidRequest = SubscriptionRequest(artiProfileId = 99999L)

                    val result = mockMvc.perform(
                        post("/api/subscriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest))
                            .with(user(fan.id.toString()).roles("FAN"))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.NOT_FOUND.value()
                    val root = objectMapper.readTree(result.contentAsString)
                    root["code"].asText() shouldBe "ARTIST_NOT_FOUND"
                }
            }
        }

        describe("구독 취소 E2E 시나리오") {

            context("구독 후 취소") {
                it("구독이 취소되고 목록에서 제거됨") {
                    // 3번째 아티스트 프로필 생성 (이 테스트 전용)
                    val tempArtiProfileId = subscriptionFixture.createArtiProfile(
                        artistName = "Temp Artist",
                        artist = artist,
                        genre = listOf("ROCK"),
                        description = "임시 아티스트"
                    )

                    // 구독 생성
                    subscriptionFixture.subscribe(fan, tempArtiProfileId)

                    // 목록 조회 전 - 구독이 포함되어 있는지 확인
                    val beforeResult = mockMvc.perform(
                        get("/api/subscriptions")
                            .with(user(fan.id.toString()).roles("FAN"))
                    ).andReturn().response

                    val beforeRoot = objectMapper.readTree(beforeResult.contentAsString)
                    val beforeHasSubscription = beforeRoot["data"].any { it["artiProfileId"].asLong() == tempArtiProfileId }
                    beforeHasSubscription shouldBe true

                    // 구독 취소
                    val unsubscribeResult = mockMvc.perform(
                        delete("/api/subscriptions/$tempArtiProfileId")
                            .with(user(fan.id.toString()).roles("FAN"))
                    ).andReturn().response

                    unsubscribeResult.status shouldBe HttpStatus.OK.value()
                    val unsubscribeRoot = objectMapper.readTree(unsubscribeResult.contentAsString)
                    unsubscribeRoot["success"].asBoolean() shouldBe true
                    unsubscribeRoot["message"].asText() shouldBe "구독이 취소되었습니다."

                    // 목록 조회 후 - 구독이 제거되었는지 확인
                    val afterResult = mockMvc.perform(
                        get("/api/subscriptions")
                            .with(user(fan.id.toString()).roles("FAN"))
                    ).andReturn().response

                    afterResult.status shouldBe HttpStatus.OK.value()
                    val afterRoot = objectMapper.readTree(afterResult.contentAsString)
                    val afterHasSubscription = afterRoot["data"].any { it["artiProfileId"].asLong() == tempArtiProfileId }
                    afterHasSubscription shouldBe false
                }
            }

            context("구독하지 않은 아티스트 구독 취소 시도") {
                it("404 Not Found 반환") {
                    val result = mockMvc.perform(
                        delete("/api/subscriptions/$artiProfileId3")
                            .with(user(fan.id.toString()).roles("FAN"))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.NOT_FOUND.value()
                    val root = objectMapper.readTree(result.contentAsString)
                    root["code"].asText() shouldBe "SUBSCRIPTION_NOT_FOUND"
                }
            }
        }

        describe("빈 구독 목록 조회") {

            context("구독이 없는 상태에서 목록 조회") {
                it("200 OK와 빈 배열 반환") {
                    // fan2는 아무 것도 구독하지 않은 상태
                    val result = mockMvc.perform(
                        get("/api/subscriptions")
                            .with(user(fan2.id.toString()).roles("FAN"))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.OK.value()
                    val root = objectMapper.readTree(result.contentAsString)
                    root["success"].asBoolean() shouldBe true
                    root["data"].isArray shouldBe true
                    root["data"].size() shouldBe 0
                }
            }
        }
    }
}
