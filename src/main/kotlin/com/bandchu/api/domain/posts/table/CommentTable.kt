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

    // 대댓글을 위한 부모 참조 (null이면 최상위 댓글)
    val parentId = long("parent_id").references(id, onDelete = ReferenceOption.CASCADE).nullable()

    override val primaryKey = PrimaryKey(id)
}
