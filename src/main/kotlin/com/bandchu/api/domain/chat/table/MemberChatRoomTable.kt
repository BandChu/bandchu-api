package com.bandchu.api.domain.chat.table

import com.bandchu.api.domain.member.table.MemberTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object MemberChatRoomTable : Table("member_chat_rooms") {
    val id = long("id").autoIncrement()
    val roomId = long("room_id").references(ChatRoomTable.id)
    val memberId = reference("member_id", MemberTable.id, onDelete = ReferenceOption.CASCADE)
    val role = varchar("role", 20)
    val lastReadMessageId = long("last_read_message_id").nullable()
    val joinedAt = timestampWithTimeZone("joined_at")

    override val primaryKey = PrimaryKey(id)
}
