package com.bandchu.api.domain.subscription.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class SubscriptionRequest(
    @field:NotNull(message = "아티스트 프로필 ID는 필수입니다.")
    @field:Positive(message = "아티스트 프로필 ID는 양수여야 합니다.")
    val artiProfileId: Long
)

