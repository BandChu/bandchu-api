package com.bandchu.api.domain.posts.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object CommentTable : Table("comments") {

    val id = integer("comment_id").autoIncrement()

    val postId = long("post_id").references(PostTable.id)

    val content = varchar("content", 500)

    val createdAt = datetime("created_at")

    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}