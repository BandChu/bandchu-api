package com.bandchu.api.domain.artists.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
class ArtistApiTest (
    private val mockMvcTester: MockMvcTester,
    private val objectMapper: ObjectMapper,
) : DescribeSpec({

    // TO DO: 사용자 생성 후 정보를 받아오는 MemberFixture 필요

    describe("전체 아티스트 목록 조회") {
        context("유효한 요청인 경우") {
            it("성공(200)과 지정한 응답 포맷을 반환한다") {
                val result = mockMvcTester
                    .get()
                    .uri("/api/artists")
                    .exchange()

                result.response.status shouldBe 200

                val apiResponseJson = objectMapper.readTree(result.response.contentAsString)
                apiResponseJson["data"]["artists"].isArray shouldBe true
            }
        }
    }

    describe("아티스트 및 공연 검색") {
        context("검색 키워드를 포함한 유효한 요청인 경우") {
            it("성공(200)과 지정한 응답 포맷을 반환한다") {
                val result = mockMvcTester
                    .get()
                    .uri("/api/artists/search?keyword=리도어")
                    .exchange()

                result.response.status shouldBe 200

                val apiResponseJson = objectMapper.readTree(result.response.contentAsString)
                val dataNode = apiResponseJson["data"]
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
//                val result = mockMvcTester
//                    .get()
//                    .uri("/api/artists/1")
//                    .exchange()
//
//                result.response.status shouldBe 200
//
//                val apiResponseJson = objectMapper.readTree(result.response.contentAsString)
//                val dataNode = apiResponseJson["data"]
//                dataNode["artistId"] shouldBe 1L
//                dataNode["name"] shouldBe "아티스트 1"
            }
        }

        context("존재하지 않는 아티 프로필에 대한 요청인 경우") {
            it("요청한 리소스를 찾을 수 없음(404)와 지정한 에러 포맷을 반환한다") {
                val result = mockMvcTester
                    .get()
                    .uri("/api/artists/1")
                    .exchange()

                result.response.status shouldBe 404

                val apiResponseJson = objectMapper.readTree(result.response.contentAsString)
                apiResponseJson["title"].asText() shouldBe "Not Found"
                apiResponseJson["detail"].asText() shouldBe "요청한 아티 프로필을 찾을 수 없습니다."
                apiResponseJson["code"].asText() shouldBe "ARTIST_NOT_FOUND"
            }
        }
    }

    describe("아티 프로필 수정") {
        context("접속한 사용자의 아티 프로필이며, 유효한 요청일 경우") {
            it("성공(200)과 지정한 응답 포맷을 반환한다") {
//                val result = mockMvcTester
//                    .patch()
//                    .uri("/api/artists/1")
//                    .exchange()
//
//                result.response.status shouldBe 200
//
//                val apiResponseJson = objectMapper.readTree(result.response.contentAsString)
//                val dataNode = apiResponseJson["data"]
//                dataNode["artistId"] shouldBe 1L
//                dataNode["name"] shouldBe "아티스트 1"
            }
        }

        context("존재하지 않는 아티 프로필에 대한 요청인 경우") {
            it("요청한 리소스를 찾을 수 없음(404)와 지정한 에러 포맷을 반환한다") {
                val result = mockMvcTester
                    .get()
                    .uri("/api/artists/1")
                    .exchange()

                result.response.status shouldBe 404

                val apiResponseJson = objectMapper.readTree(result.response.contentAsString)
                apiResponseJson["title"].asText() shouldBe "Not Found"
                apiResponseJson["detail"].asText() shouldBe "요청한 아티 프로필을 찾을 수 없습니다."
                apiResponseJson["code"].asText() shouldBe "ARTIST_NOT_FOUND"
            }
        }
    }

        context("접속한 사용자의 아티 프로필이 아닐 경우") {
            it("권한 없음(403)와 지정한 에러 포맷을 반환한다") {
//                var memberId = 1
//                val result = mockMvcTester
//                    .get()
//                    .uri("/api/artists/2")
//                    .exchange()
//
//                result.response.status shouldBe 403
//
//                val apiResponseJson = objectMapper.readTree(result.response.contentAsString)
//                apiResponseJson["title"].asText() shouldBe "Forbidden"
//                apiResponseJson["detail"].asText() shouldBe "해당 아티프로필에 대한 접근 권한이 없습니다."
//                apiResponseJson["code"].asText() shouldBe "ARTIST_FORBIDDEN"
//            }
        }
    }
})