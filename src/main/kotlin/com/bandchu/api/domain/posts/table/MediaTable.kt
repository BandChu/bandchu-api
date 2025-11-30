package com.bandchu.api.domain.posts.table

import com.bandchu.api.domain.posts.model.Media
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object MediaTable : Table("medias") {


    val id = integer("media_id").autoIncrement()
    val postId = long("post_id").references(PostTable.id)

    val s3Url = varchar("s3_url", 255)
    val fileSize = varchar("file_size", 50)

    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}

fun ResultRow.toMedia(): Media =
    Media(
        mediaId = this[MediaTable.id],
        postId = this[MediaTable.postId],
        s3Url = this[MediaTable.s3Url],
        fileSize = this[MediaTable.fileSize],
        createdAt = this[MediaTable.createdAt],
        updatedAt = this[MediaTable.updatedAt]
    )
