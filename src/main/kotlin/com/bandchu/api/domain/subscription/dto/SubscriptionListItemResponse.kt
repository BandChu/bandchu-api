package com.bandchu.api.domain.subscription.dto

import io.swagger.v3.oas.annotations.media.Schema

data class SubscriptionListItemResponse(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val artiProfileId: Long,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val artistName: String,

    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    val profileImage: String
)

