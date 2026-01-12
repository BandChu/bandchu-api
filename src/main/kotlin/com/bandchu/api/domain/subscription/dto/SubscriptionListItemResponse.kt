package com.bandchu.api.domain.subscription.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "구독리스트 아이템 응답")
data class SubscriptionListItemResponse(
    @get:Schema(description = "아티프로필 ID", example = "101")
    val artiProfileId: Long,

    @get:Schema(description = "아티스트 이름", example = "데이먼스이어")
    val artistName: String,

    @get:Schema(description = " 프로필 이미지 url", example = "s3://www.s23.com")
    val profileImage: String
)

