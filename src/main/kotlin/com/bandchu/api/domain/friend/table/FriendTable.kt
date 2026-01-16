package com.bandchu.api.domain.friend.table

import com.bandchu.api.domain.member.table.MemberTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone


object FriendTable : Table("friends") {
    val id = long("friend_id").autoIncrement()

    val senderId = long("sender_id")
        .references(MemberTable.id, onDelete = ReferenceOption.CASCADE)
    val receiverId = long("receiver_id")
        .references(MemberTable.id, onDelete = ReferenceOption.CASCADE)

    val status = enumerationByName("status", 10, FriendStatus::class)
        .default(FriendStatus.PENDING) // 기본값은 PENDING

    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)

    // senderId와 receiverId 조합 유니크 (중복 친구 요청 방지)
    init {
        index(true, senderId, receiverId)
    }
}