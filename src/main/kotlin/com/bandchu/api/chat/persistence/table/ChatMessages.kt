package com.bandchu.api.chat.persistence.table

import com.bandchu.api.chat.domain.MessageType
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object ChatMessages : Table("chat_message") {
    val id = long("id").autoIncrement()
    val sender = reference("sender_id", Members)
    val room = reference("room_id", ChatRooms)
    val messageType = enumerationByName("message_type", 10, MessageType::class)
    val content = varchar("content", 1000).nullable()
    val fileUrl = varchar("file_url", 255).nullable()
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}