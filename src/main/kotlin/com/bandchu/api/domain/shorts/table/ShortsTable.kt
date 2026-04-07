package com.bandchu.api.domain.shorts.table

import com.bandchu.api.domain.artist.table.ArtiProfileTable
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object ShortsTable : LongIdTable("shorts") {
    val artistProfileId = reference("artist_profile_id", ArtiProfileTable)
    val title = varchar("title", 200)
    val description = text("description").nullable()
    val videoUrl = text("video_url") // S3/CloudFront HLS 주소
    val thumbnailUrl = text("thumbnail_url").nullable()
    val viewCount = long("view_count").default(0)
    val createdAt = timestampWithTimeZone("created_at")
    val shareCount = long("share_count").default(0)
}