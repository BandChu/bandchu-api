package com.bandchu.api.domain.posts.table

import com.bandchu.api.domain.member.table.MemberTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object LikeTable : Table("likes") {
    val id = long("like_id").autoIncrement()

    // 누가 좋아요를 눌렀는가
    val memberId = reference("member_id", MemberTable.id, onDelete = ReferenceOption.CASCADE)

    // 무엇을(POST, COMMENT 등) 좋아요 했는가
    val targetType = enumerationByName("target_type", 20, LikeTargetType::class)

    // 대상의 PK (post_id 혹은 comment_id)
    val targetId = long("target_id")

    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)

    init {
        // [중요] 한 유저가 같은 대상에 중복 좋아요를 하지 못하도록 복합 유니크 인덱스 설정
        index(true, memberId, targetType, targetId)
    }
}