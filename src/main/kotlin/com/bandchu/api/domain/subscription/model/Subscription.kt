package com.bandchu.api.domain.subscription.model

import kotlinx.datetime.LocalDateTime

data class Subscription(
    val id: Long? = null,
    val memberId: Long,
    val artProfileId: Long,
    val createdAt: LocalDateTime? = null
)

