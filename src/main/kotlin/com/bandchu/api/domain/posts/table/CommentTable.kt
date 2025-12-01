package com.bandchu.api.domain.posts.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object CommentTable : Table("comment") {
    val id = long("comment_id").autoIncrement()
    val content = varchar("content", length = 100)

    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    val postId = long("post_id")
        .references(PostTable.id)

    override val primaryKey = PrimaryKey(id)
}
