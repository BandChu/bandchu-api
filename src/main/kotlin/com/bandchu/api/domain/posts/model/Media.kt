package com.bandchu.api.domain.posts.model

import kotlinx.datetime.LocalDateTime

data class Media (
    val media_id: Int? = null,
    val s3_url: String? = null,
    val created_at: LocalDateTime? = null,
    val s3_file_size: String? = null,

    )