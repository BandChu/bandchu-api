package com.bandchu.api.domain.chat.table

import com.bandchu.api.domain.member.table.MemberTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object ChatMessageTable : Table("chat_messages") {
    val id = long("id").autoIncrement()
    val roomId = long("room_id").references(ChatRoomTable.id)
    val senderId = long("sender_id").references(MemberTable.id)
    val messageType = enumerationByName("message_type", 10, MessageType::class)
    val content = varchar("content", 1000).nullable()
    val fileUrl = varchar("file_url", 255).nullable()
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}