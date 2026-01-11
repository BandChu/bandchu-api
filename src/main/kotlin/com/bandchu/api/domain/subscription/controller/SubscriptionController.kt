package com.bandchu.api.domain.subscription.controller

import com.bandchu.api.domain.subscription.dto.SubscriptionListItemResponse
import com.bandchu.api.domain.subscription.dto.SubscriptionRequest
import com.bandchu.api.domain.subscription.dto.SubscriptionResponse
import com.bandchu.api.domain.subscription.service.SubscriptionService
import com.bandchu.api.global.response.ApiResponse
import com.bandchu.api.global.util.getCurrentUserId
import com.bandchu.api.global.util.toOffsetDateTime
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.time.ZoneOffset

@RestController
@RequestMapping("/api/subscriptions")
@Tag(name = "Subscription", description = "구독 관련 API")
class SubscriptionController(
    private val subscriptionService: SubscriptionService
) {
    private val log = LoggerFactory.getLogger(javaClass)
    @Operation(summary = "앨범 일부 삭제", description = "앨범을 앨범 ID를 통해 삭제합니다")
    @PostMapping
    fun subscribe(@Valid @RequestBody request: SubscriptionRequest): ResponseEntity<ApiResponse<SubscriptionResponse>> {
        val memberId = getCurrentUserId()

        val subscription = subscriptionService.subscribe(memberId, request.artiProfileId)

        val subscriptionId = subscription.id ?: run {
            log.error("Critical: Subscription ID is null after save. MemberId: $memberId, ArtiProfileId: ${request.artiProfileId}")
            throw IllegalStateException("구독 ID가 없습니다.")
        }

        val createdAt = subscription.createdAt?.toOffsetDateTime() ?: OffsetDateTime.now(ZoneOffset.UTC)

        val response = SubscriptionResponse(
            subscriptionId = subscriptionId,
            memberId = subscription.memberId,
            artiProfileId = subscription.artiProfileId,
            createdAt = createdAt
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "아티스트를 구독했습니다."))
    }
    @Operation(summary = "앨범 일부 삭제", description = "앨범을 앨범 ID를 통해 삭제합니다")
    @DeleteMapping("/{artiProfileId}")
    fun unsubscribe(@PathVariable artiProfileId: Long): ResponseEntity<ApiResponse<Unit>> {
        val memberId = getCurrentUserId()

        subscriptionService.unsubscribe(memberId, artiProfileId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(Unit, "구독이 취소되었습니다."))
    }
    @Operation(summary = "앨범 일부 삭제", description = "앨범을 앨범 ID를 통해 삭제합니다")
    @GetMapping
    fun getSubscriptions(): ResponseEntity<ApiResponse<List<SubscriptionListItemResponse>>> {
        val memberId = getCurrentUserId()

        val subscriptions = subscriptionService.getSubscriptions(memberId)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(subscriptions, "구독 목록 조회 성공"))
    }
}

