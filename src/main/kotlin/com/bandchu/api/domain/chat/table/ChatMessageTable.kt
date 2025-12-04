package com.bandchu.api.domain.chat.table

import com.bandchu.api.domain.member.table.MemberTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object ChatMessageTable : Table("chat_messages") {
    val id = long("id").autoIncrement()
    val roomId = long("room_id").references(ChatRoomTable.id)
    val senderId = reference("sender_id", MemberTable.id, onDelete = ReferenceOption.CASCADE)
    val messageType = enumerationByName("message_type", 10, MessageType::class)
    val content = varchar("content", 1000).nullable()
    val fileUrl = varchar("file_url", 255).nullable()
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)
}