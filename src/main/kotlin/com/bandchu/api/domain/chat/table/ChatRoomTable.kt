package com.bandchu.api.domain.chat.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object ChatRoomTable : Table("chat_rooms") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 255).nullable()
    val roomType = enumerationByName("room_type", 50, RoomType::class)  // enumeration -> enumerationByName
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)
}
