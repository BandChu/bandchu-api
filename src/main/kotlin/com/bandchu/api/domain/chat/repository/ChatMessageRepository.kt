package com.bandchu.api.domain.chat.repository

import com.bandchu.api.chat.dto.ChatMessageResponse
import com.bandchu.api.chat.dto.SendMessageRequest
import com.bandchu.api.domain.chat.table.ChatMessageTable
import com.bandchu.api.domain.chat.table.MemberChatRoomTable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import kotlin.text.get

@Repository
class ChatMessageRepository{
    fun saveMessage(roomId: Long, senderId: Long, req: SendMessageRequest): ChatMessageResponse {
        return transaction {
            //DB에 요청 메시지를 저장함
            val message = ChatMessageTable.insert {
                it[ChatMessageTable.roomId] = roomId
                it[ChatMessageTable.senderId] = senderId
                it[messageType] = req.messageType
                it[content] = req.content
                it[fileUrl] = req.fileUrl
                it[createdAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            }.resultedValues?.firstOrNull()
                ?: throw IllegalStateException("메시지 저장 실패")  // null 체크 추가

            //DB에서 저장된 메시지를 조회해서 가져옴
            ChatMessageTable
                .selectAll()
                .where{ ChatMessageTable.id eq message[ChatMessageTable.id] }
                .single()
                .let{ ChatMessageResponse.from(it) }
        }
    }

    fun isRoomMember(roomId: Long, memberId: Long): Boolean {
        return MemberChatRoomTable.selectAll()
            .where{(MemberChatRoomTable.roomId eq roomId) and  (MemberChatRoomTable.memberId eq memberId) }
            .any()
    }
}