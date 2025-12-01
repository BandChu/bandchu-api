package com.bandchu.api.domain.posts.dto.response

import com.bandchu.api.domain.posts.table.MediaTable
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.OffsetDateTime

data class MediaResponse(
    val mediaId: Long,
    val postId: Long,
    val s3Url: String,
    val fileSize: Long,
    val createdAt: OffsetDateTime,
)

fun ResultRow.toMediaResponse(): MediaResponse = MediaResponse(
    mediaId = this[MediaTable.id],
    postId = this[MediaTable.postId],
    s3Url = this[MediaTable.s3Url],
    fileSize = this[MediaTable.s3FileSize],
    createdAt = this[MediaTable.createdAt]
)
