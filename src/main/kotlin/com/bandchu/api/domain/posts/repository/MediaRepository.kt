package com.bandchu.api.domain.posts.repository

import com.bandchu.api.domain.posts.dto.request.MediaRequest
import com.bandchu.api.domain.posts.dto.response.CreateMediaResponse
import com.bandchu.api.domain.posts.dto.response.toMediaResponse
import com.bandchu.api.domain.posts.table.MediaTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class MediaRepository {
    fun findByPostId(postId: Long): List<CreateMediaResponse> = transaction {
        MediaTable
            .selectAll()
             .where{ MediaTable.postId eq postId }
            .map { it.toMediaResponse() }
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    fun insertMediaList(postId: Long, mediaList: List<MediaRequest>): List<CreateMediaResponse> = transaction {
        val now = OffsetDateTime.now(java.time.ZoneOffset.UTC)

        mediaList.map { mediaRequest ->
            // MediaTable에 Insert
            val insertedRow = MediaTable.insert { row ->
                row[MediaTable.postId] = postId
                row[MediaTable.s3Url] = mediaRequest.s3Url
                row[MediaTable.s3FileSize] = mediaRequest.fileSize
                row[MediaTable.createdAt] = now
            }

            // Insert된 Row 조회 후 DTO로 변환
            MediaTable
                .selectAll()
                .where { MediaTable.id eq insertedRow[MediaTable.id] }
                .single()
                .toMediaResponse()
        }
    }

    // S3 URL과 파일 사이즈로 미디어 저장 후 CreateMediaResponse 반환
    fun save(postId: Long, s3Url: String, fileSize: Long): CreateMediaResponse = transaction {
        val now = java.time.OffsetDateTime.now()

        val insertedRow = MediaTable.insert { row ->
            row[MediaTable.postId] = postId
            row[MediaTable.s3Url] = s3Url
            row[MediaTable.s3FileSize] = fileSize
            row[MediaTable.createdAt] = now
        }

        // Insert된 Row 조회 후 MediaResponse 반환
        MediaTable
            .selectAll()
            .where { MediaTable.id eq insertedRow[MediaTable.id] }
            .single()
            .toMediaResponse()
    }
}