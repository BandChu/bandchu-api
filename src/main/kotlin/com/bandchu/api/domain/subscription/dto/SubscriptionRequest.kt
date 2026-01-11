package com.bandchu.api.domain.subscription.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class SubscriptionRequest(
    @get:Schema(description = "공연명 이름", example = "데이먼스이어 단독콘서트")
    @field:NotNull(message = "아티스트 프로필 ID는 필수입니다.")
    @field:Positive(message = "아티스트 프로필 ID는 양수여야 합니다.")
    val artiProfileId: Long
)

