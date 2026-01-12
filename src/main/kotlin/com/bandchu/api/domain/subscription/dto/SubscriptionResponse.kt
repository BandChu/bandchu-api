package com.bandchu.api.domain.subscription.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "구독 응답")
data class SubscriptionResponse(
    @get:Schema(description = "구독 객체 고유 ID", example = "102")
    val subscriptionId: Long,

    @get:Schema(description = "멤버 객체 고유 ID", example = "111")
    val memberId: Long,

    @get:Schema(description = "아티프로필 고유 ID", example = "1212")
    val artiProfileId: Long,

    @get:Schema(description = "구독 생성 시각", example = "2022:02:02")
    val createdAt: OffsetDateTime
)

