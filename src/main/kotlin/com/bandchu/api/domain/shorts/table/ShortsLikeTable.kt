package com.bandchu.api.domain.shorts.table

import com.bandchu.api.domain.member.table.MemberTable
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone


object ShortsLikeTable : LongIdTable("shorts_likes") {
    val shortsId = reference("shorts_id", ShortsTable).index()
    val memberId = reference("member_id", MemberTable)
    val createdAt = timestampWithTimeZone("created_at")

    init {
        // 한 유저가 한 영상에 좋아요 한 번만 가능하도록 유니크 제약
        uniqueIndex(shortsId, memberId)
    }
}