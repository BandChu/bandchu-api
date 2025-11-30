package com.bandchu.api.domain.posts.repository

import com.bandchu.api.domain.posts.model.Media
import com.bandchu.api.domain.posts.table.MediaTable
import com.bandchu.api.domain.posts.table.toMedia
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import kotlin.time.Clock

@Repository
class MediaRepository {
    fun findByPostId(postId: Long): List<Media> = transaction {
        MediaTable
            .selectAll()
             .where{ MediaTable.postId eq postId }
            .map { it.toMedia() }
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    fun insertMediaList(postId: Long, mediaList: List<Media>): List<Media> = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        mediaList.map { media ->
            val inserted = MediaTable.insert { row ->
                row[MediaTable.postId] = postId
                row[MediaTable.s3Url] = media.s3Url
                row[MediaTable.fileSize] = media.fileSize
               row[MediaTable.createdAt] = now
                row[MediaTable.updatedAt] = now
            }

            val id = inserted[MediaTable.id]

            Media(
                mediaId = id,
                postId = postId,
                s3Url = media.s3Url,
                fileSize = media.fileSize,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

fun ResultRow.toMedia(): Media =
    Media(
        mediaId = this[MediaTable.id],
        postId = this[MediaTable.postId],
        s3Url = this[MediaTable.s3Url],
        fileSize = this[MediaTable.fileSize],
        createdAt = this[MediaTable.createdAt].toLocalDateTime(),
        updatedAt = this[MediaTable.updatedAt].toKotlinLocalDateTime()
    )