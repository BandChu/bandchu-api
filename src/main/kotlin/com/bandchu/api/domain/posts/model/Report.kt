package com.bandchu.api.domain.posts.model

import kotlinx.datetime.LocalDateTime
import org.hibernate.validator.internal.util.privilegedactions.LoadClass

data class Report (
    val report_id: Int? = null,
    val reason: String? = null,
    val reported_at: LocalDateTime? = null,
)