package com.bandchu.api.domain.member.model

import kotlinx.datetime.LocalDateTime

data class Subscription (
    val subscription_id: Int,
    val created_at: LocalDateTime,
)