package com.bandchu.api.domain.subscription.controller

import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.domain.subscription.dto.SubscriptionListItemResponse
import com.bandchu.api.domain.subscription.dto.SubscriptionRequest
import com.bandchu.api.domain.subscription.dto.SubscriptionResponse
import com.bandchu.api.domain.subscription.model.Subscription
import com.bandchu.api.domain.subscription.service.SubscriptionService
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month

@TestConfiguration
class SubscriptionControllerTestConfig {
    @Bean
    fun subscriptionService(): SubscriptionService = mockk()
}

@WebMvcTest(SubscriptionController::class)
@ContextConfiguration(classes = [SubscriptionController::class, SubscriptionControllerTestConfig::class])
class SubscriptionControllerTest(
    private val mockMvc: MockMvc,
    private val subscriptionService: SubscriptionService,
    private val objectMapper: ObjectMapper
) : DescribeSpec({

    describe("POST /api/subscriptions") {
        context("FAN 역할의 유저가 유효한 아티스트 프로필 ID로 구독 요청을 보내면") {
            it("201 Created와 함께 구독 정보를 반환한다") {
                // given
                val memberId = 1L
                val artProfileId = 12L
                val subscriptionId = 88L
                val authentication = UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_FAN"))
                )
                SecurityContextHolder.getContext().authentication = authentication

                val request = SubscriptionRequest(artProfileId = artProfileId)

                val subscription = Subscription(
                    id = subscriptionId,
                    memberId = memberId,
                    artProfileId = artProfileId,
                    createdAt = LocalDateTime(2025, Month.NOVEMBER, 21, 0, 0, 0)
                )

                every { subscriptionService.subscribe(memberId, artProfileId) } returns subscription

                // when & then
                mockMvc.perform(
                    post("/api/subscriptions")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isCreated)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.subscriptionId").value(subscriptionId))
                    .andExpect(jsonPath("$.data.memberId").value(memberId))
                    .andExpect(jsonPath("$.data.artProfileId").value(artProfileId))
                    .andExpect(jsonPath("$.data.createdAt").exists())
                    .andExpect(jsonPath("$.message").value("아티스트를 구독했습니다."))
            }
        }

        context("이미 구독 중인 아티스트에 대해 구독 요청을 보내면") {
            it("409 Conflict와 함께 에러를 반환한다") {
                // given
                val memberId = 1L
                val artProfileId = 12L
                val authentication = UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_FAN"))
                )
                SecurityContextHolder.getContext().authentication = authentication

                val request = SubscriptionRequest(artProfileId = artProfileId)

                every { subscriptionService.subscribe(memberId, artProfileId) } throws com.bandchu.api.global.exception.BusinessException(
                    com.bandchu.api.global.exception.ErrorCode.SUBSCRIPTION_DUPLICATED
                )

                // when & then
                mockMvc.perform(
                    post("/api/subscriptions")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isConflict)
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.code").value("SUBSCRIPTION_DUPLICATED"))
                    .andExpect(jsonPath("$.detail").value("이미 구독 중인 아티스트입니다."))
            }
        }

        context("ARTIST 역할의 유저가 구독 요청을 보내면") {
            it("403 Forbidden과 함께 에러를 반환한다") {
                // given
                val memberId = 1L
                val artProfileId = 12L
                val authentication = UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_ARTIST"))
                )
                SecurityContextHolder.getContext().authentication = authentication

                val request = SubscriptionRequest(artProfileId = artProfileId)

                every { subscriptionService.subscribe(memberId, artProfileId) } throws com.bandchu.api.global.exception.BusinessException(
                    com.bandchu.api.global.exception.ErrorCode.INVALID_ROLE
                )

                // when & then
                mockMvc.perform(
                    post("/api/subscriptions")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isForbidden)
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.code").value("INVALID_ROLE"))
            }
        }

        context("토큰이 없이 구독 요청을 보내면") {
            it("403 Forbidden과 함께 에러를 반환한다") {
                // given
                val request = SubscriptionRequest(artProfileId = 12L)

                // when & then
                mockMvc.perform(
                    post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isForbidden)
            }
        }
    }

    describe("DELETE /api/subscriptions/{artProfileId}") {
        context("FAN 역할의 유저가 구독 중인 아티스트의 구독 취소 요청을 보내면") {
            it("200 OK와 함께 성공 메시지를 반환한다") {
                // given
                val memberId = 1L
                val artProfileId = 12L
                val authentication = UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_FAN"))
                )
                SecurityContextHolder.getContext().authentication = authentication

                every { subscriptionService.unsubscribe(memberId, artProfileId) } returns Unit

                // when & then
                mockMvc.perform(
                    delete("/api/subscriptions/$artProfileId")
                        .with(authentication(authentication))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isEmpty)
                    .andExpect(jsonPath("$.message").value("구독이 취소되었습니다."))
            }
        }

        context("구독 중이 아닌 아티스트의 구독 취소 요청을 보내면") {
            it("404 Not Found와 함께 에러를 반환한다") {
                // given
                val memberId = 1L
                val artProfileId = 12L
                val authentication = UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_FAN"))
                )
                SecurityContextHolder.getContext().authentication = authentication

                every { subscriptionService.unsubscribe(memberId, artProfileId) } throws com.bandchu.api.global.exception.BusinessException(
                    com.bandchu.api.global.exception.ErrorCode.SUBSCRIPTION_NOT_FOUND
                )

                // when & then
                mockMvc.perform(
                    delete("/api/subscriptions/$artProfileId")
                        .with(authentication(authentication))
                )
                    .andExpect(status().isNotFound)
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.code").value("SUBSCRIPTION_NOT_FOUND"))
                    .andExpect(jsonPath("$.detail").value("구독 중이 아닌 아티스트입니다."))
            }
        }

        context("토큰이 없이 구독 취소 요청을 보내면") {
            it("403 Forbidden과 함께 에러를 반환한다") {
                // given
                val artProfileId = 12L

                // when & then
                mockMvc.perform(
                    delete("/api/subscriptions/$artProfileId")
                )
                    .andExpect(status().isForbidden)
            }
        }
    }

    describe("GET /api/subscriptions") {
        context("FAN 역할의 유저가 구독 목록 조회 요청을 보내면") {
            it("200 OK와 함께 구독 목록을 반환한다") {
                // given
                val memberId = 1L
                val authentication = UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_FAN"))
                )
                SecurityContextHolder.getContext().authentication = authentication

                val subscriptionList = listOf(
                    SubscriptionListItemResponse(
                        artProfileId = 12L,
                        artistName = "NewJeans",
                        profileImage = "https://example.com/profile.jpg"
                    )
                )

                every { subscriptionService.getSubscriptions(memberId) } returns subscriptionList

                // when & then
                mockMvc.perform(
                    get("/api/subscriptions")
                        .with(authentication(authentication))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].artProfileId").value(12))
                    .andExpect(jsonPath("$.data[0].artistName").value("NewJeans"))
                    .andExpect(jsonPath("$.data[0].profileImage").value("https://example.com/profile.jpg"))
                    .andExpect(jsonPath("$.message").value("구독 목록 조회 성공"))
            }
        }

        context("구독 목록이 비어있으면") {
            it("200 OK와 함께 빈 배열을 반환한다") {
                // given
                val memberId = 1L
                val authentication = UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_FAN"))
                )
                SecurityContextHolder.getContext().authentication = authentication

                every { subscriptionService.getSubscriptions(memberId) } returns emptyList()

                // when & then
                mockMvc.perform(
                    get("/api/subscriptions")
                        .with(authentication(authentication))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isEmpty)
                    .andExpect(jsonPath("$.message").value("구독 목록 조회 성공"))
            }
        }

        context("토큰이 없이 구독 목록 조회 요청을 보내면") {
            it("403 Forbidden과 함께 에러를 반환한다") {
                // when & then
                mockMvc.perform(
                    get("/api/subscriptions")
                )
                    .andExpect(status().isForbidden)
            }
        }
    }
})

