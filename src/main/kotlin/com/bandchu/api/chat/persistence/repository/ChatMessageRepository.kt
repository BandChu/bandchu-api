package com.bandchu.api.chat.persistence.repository

import com.bandchu.api.chat.dto.ChatMessageResponse
import com.bandchu.api.chat.dto.SendMessageRequest
import com.bandchu.api.chat.persistence.table.ChatMessages
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class ChatMessageRepository{
    fun saveMessage(roomId: Long, senderId: Long, req: SendMessageRequest): ChatMessageResponse {
        return transaction {
            //DB에 요청 메시지를 저장함
            val message = ChatMessages.insert {
                it[room] = roomId
                it[sender] = senderId
                it[messageType] = req.messageType
                it[content] = req.content
                it[fileUrl] = req.fileUrl
                it[createdAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            }.resultedValues?.firstOrNull()
                ?: throw IllegalStateException("메시지 저장 실패")  // null 체크 추가

            //DB에서 저장된 메시지를 조회해서 가져옴
            ChatMessages
                .selectAll()
                .where{ ChatMessages.id eq message[ChatMessages.id] }
                .single()
                .let{ ChatMessageResponse.from(it) }
        }
    }

    fun isRoomMember(roomId: Long, memberId: Long): Boolean {
        //채팅방 아이디와 멤버 아이디로 채팅방에 속해있는지 검증
//       ChatRoomMembers.select {
//            (ChatRoomMembers.room eq roomId) and (ChatRoomMembers.member eq memberId)
//        }.any()
        return true
    }
}