package com.bandchu.api.domain.subscription.dto

import java.time.OffsetDateTime

data class SubscriptionResponse(
    val subscriptionId: Long,
    val memberId: Long,
    val artiProfileId: Long,
    val createdAt: OffsetDateTime
)

