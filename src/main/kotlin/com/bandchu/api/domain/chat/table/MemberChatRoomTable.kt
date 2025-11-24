package com.bandchu.api.domain.chat.table

import com.bandchu.api.domain.member.table.MemberTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object MemberChatRoomTable : Table("member_chat_rooms") {
    val id = long("id").autoIncrement()
    val roomId = long("room_id").references(ChatRoomTable.id)
    val userId = long("user_id").references(MemberTable.id)
    val role = varchar("role", 20)
    val joinedAt = datetime("joined_at")

    override val primaryKey = PrimaryKey(id)
}