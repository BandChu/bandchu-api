package com.bandchu.api.domain.chat.repository

import com.bandchu.api.domain.chat.model.ChatRoom
import com.bandchu.api.domain.chat.table.ChatRoomTable
import com.bandchu.api.domain.chat.table.RoomType
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class ChatRoomRepository {
    /**
     * 채팅방 생성
     */
    fun create(name: String?, roomType: RoomType, createdAt: OffsetDateTime): Long {
        return transaction {
            ChatRoomTable.insert {
                it[ChatRoomTable.name] = name
                it[ChatRoomTable.roomType] = roomType
                it[ChatRoomTable.createdAt] = createdAt
            } get ChatRoomTable.id
        }
    }

    /**
     * 채팅방 ID로 조회
     */
    fun findById(roomId: Long): ChatRoom? {
        return transaction {
            ChatRoomTable.selectAll()
                .where { ChatRoomTable.id eq roomId }
                .singleOrNull()
                ?.let { row ->
                    ChatRoom(
                        id = row[ChatRoomTable.id],
                        name = row[ChatRoomTable.name],
                        roomType = row[ChatRoomTable.roomType],
                        createdAt = row[ChatRoomTable.createdAt]
                    )
                }
        }
    }

    /**
     * 여러 채팅방 ID로 조회
     */
    fun findByIds(roomIds: List<Long>): List<ChatRoom> {
        if (roomIds.isEmpty()) return emptyList()
        return transaction {
            roomIds.mapNotNull { roomId -> findById(roomId) }
        }
    }
}
