package com.bandchu.api.domain.shorts.table

import com.bandchu.api.domain.member.table.MemberTable
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object ShortsCommentTable : LongIdTable("shorts_comments") {
    val shortsId = reference("shorts_id", ShortsTable).index()
    val memberId = reference("member_id", MemberTable)
    val content = text("content")
    val createdAt = timestampWithTimeZone("created_at")
}