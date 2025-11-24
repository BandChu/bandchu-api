package com.bandchu.api.domain.artists.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.assertj.MockMvcTester

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ArtistApiTest (
    private val mockMvcTester: MockMvcTester,
    private val objectMapper: ObjectMapper,
) : DescribeSpec({

    describe("전체 아티스트 목록 조회") {
        context("유효한 요청인 경우") {
            it("성공(200)과 지정한 응답 포맷을 반환한다") {
                val result = mockMvcTester
                    .get()
                    .uri("/api/artists")
                    .exchange()

                result.response.status shouldBe 200

                val apiResponseJson = objectMapper.readTree(result.response.contentAsString)
                apiResponseJson.get("data").get("artists").isArray shouldBe true
            }
        }
    }
})