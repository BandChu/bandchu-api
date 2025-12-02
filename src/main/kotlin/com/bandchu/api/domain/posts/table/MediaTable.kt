package com.bandchu.api.domain.posts.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object MediaTable : Table("media") {

    val id = long("media_id").autoIncrement()

    val postId = long("post_id")
        .references(PostTable.id)

    val s3Url = varchar("s3_url", length = 500)

    val s3FileSize = long("s3_file_size")

    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)
}
