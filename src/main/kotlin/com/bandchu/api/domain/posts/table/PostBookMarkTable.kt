package com.bandchu.api.domain.posts.table

import com.bandchu.api.domain.member.table.MemberTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

// 북마크 테이블
object PostBookmarkTable : Table("post_bookmarks") {
    val id = long("post_bookmark_id").autoIncrement()
    val postId = reference("post_id", PostTable.id, onDelete = ReferenceOption.CASCADE)
    val memberId = reference("member_id", MemberTable.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(true, postId, memberId)
    }
}