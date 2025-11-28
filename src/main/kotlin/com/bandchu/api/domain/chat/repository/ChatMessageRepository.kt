package com.bandchu.api.domain.chat.repository

import com.bandchu.api.domain.chat.dto.ChatMessageResponse
import com.bandchu.api.domain.chat.dto.SendMessageRequest
import com.bandchu.api.domain.chat.table.ChatMessageTable
import com.bandchu.api.domain.chat.table.MemberChatRoomTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class ChatMessageRepository {
    
    fun saveMessage(roomId: Long, senderId: Long, req: SendMessageRequest): ChatMessageResponse {
        //DB에 요청 메시지를 저장함
        val message = ChatMessageTable.insert {
            it[ChatMessageTable.roomId] = roomId
            it[ChatMessageTable.senderId] = senderId
            it[messageType] = req.messageType
            it[content] = req.content
            it[fileUrl] = req.fileUrl
            it[createdAt] = OffsetDateTime.now(java.time.ZoneOffset.UTC)
        }.resultedValues?.firstOrNull()
            ?: throw IllegalStateException("메시지 저장 실패")  // null 체크 추가
        
        //DB에서 저장된 메시지를 조회해서 가져옴
        return ChatMessageTable
            .selectAll()
            .where{ ChatMessageTable.id eq message[ChatMessageTable.id] }
            .single()
            .let{ ChatMessageResponse.from(it) }
    }
    
    fun isRoomMember(roomId: Long, memberId: Long): Boolean {
        return MemberChatRoomTable.selectAll()
            .where{(MemberChatRoomTable.roomId eq roomId) and  (MemberChatRoomTable.memberId eq memberId) }
            .any()
    }
    
    fun fetchMessages(roomId: Long, cursor: Long?, size: Int): List<ChatMessageResponse> {
        val query = ChatMessageTable
            .selectAll()
            .where { ChatMessageTable.roomId eq roomId }
            .apply {
                if (cursor != null) {
                    andWhere { ChatMessageTable.id less cursor }
                }
            }
            .orderBy(ChatMessageTable.id, SortOrder.DESC)
            .limit(size)
        
        return query.map { ChatMessageResponse.from(it) }
            .reversed() //시간 순으로
    }

    /**
     * 채팅방의 마지막 메시지 조회 (채팅방 목록에서 사용)
     * @return 마지막 메시지 또는 null (메시지가 없는 경우)
     */
    fun findLastMessageByRoomId(roomId: Long): ChatMessageResponse? {
        return ChatMessageTable
            .selectAll()
            .where { ChatMessageTable.roomId eq roomId }
            .orderBy(ChatMessageTable.id, SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.let { ChatMessageResponse.from(it) }
    }

    /**
     * 읽지 않은 메시지 개수 계산
     * @param roomId 채팅방 ID
     * @param afterMessageId 이 메시지 ID 이후의 메시지들
     * @param excludeSenderId 제외할 발신자 ID (본인 메시지 제외)
     * @return 읽지 않은 메시지 개수
     */
    fun countUnreadMessages(roomId: Long, afterMessageId: Long, excludeSenderId: Long): Int {
        return ChatMessageTable
            .select(ChatMessageTable.id)
            .where {
                (ChatMessageTable.roomId eq roomId) and
                (ChatMessageTable.id greater afterMessageId) and
                (ChatMessageTable.senderId neq excludeSenderId)
            }
            .count()
            .toInt()
    }

    /**
     * 채팅방의 전체 메시지 개수 (본인 제외)
     * @param roomId 채팅방 ID
     * @param excludeSenderId 제외할 발신자 ID (본인 메시지 제외)
     * @return 전체 메시지 개수
     */
    fun countAllMessages(roomId: Long, excludeSenderId: Long): Int {
        return ChatMessageTable
            .select(ChatMessageTable.id)
            .where {
                (ChatMessageTable.roomId eq roomId) and
                (ChatMessageTable.senderId neq excludeSenderId)
            }
            .count()
            .toInt()
    }
}
