package com.bandchu.api.domain.posts.table

import com.bandchu.api.domain.member.table.MemberTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object CommentTable : Table("comment") {
    val id = long("comment_id").autoIncrement()
    val content = varchar("content", length = 100)

    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    val postId = long("post_id")
        .references(PostTable.id)

    val memberId = reference("member_id", MemberTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(id)
}
