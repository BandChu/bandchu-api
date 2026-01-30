package com.bandchu.api.fixture

import com.bandchu.api.domain.chat.dto.CreateChatRoomRequest
import com.bandchu.api.domain.chat.dto.CreateChatRoomResponse
import com.bandchu.api.domain.chat.dto.SendMessageRequest
import com.bandchu.api.domain.chat.dto.ChatMessageResponse
import com.bandchu.api.domain.chat.service.ChatMessageService
import com.bandchu.api.domain.chat.service.ChatRoomService
import com.bandchu.api.domain.chat.table.MessageType
import com.bandchu.api.domain.chat.table.RoomType
import com.bandchu.api.domain.member.model.Member

class ChatFixture(
    private val chatRoomService: ChatRoomService,
    private val chatMessageService: ChatMessageService,
    private val authFixture: AuthFixture
) {

    /**
     * DIRECT 채팅방 생성 (1:1)
     */
    fun createDirectRoom(
        creator: Member,
        otherMember: Member
    ): CreateChatRoomResponse {
        authFixture.authenticateAs(creator)

        val request = CreateChatRoomRequest(
            roomType = RoomType.DIRECT,
            name = null,
            memberIds = listOf(otherMember.id!!)
        )

        return chatRoomService.createChatRoom(request, creator.id!!)
    }

    /**
     * GROUP 채팅방 생성
     */
    fun createGroupRoom(
        creator: Member,
        members: List<Member>,
        roomName: String = "테스트 그룹"
    ): CreateChatRoomResponse {
        authFixture.authenticateAs(creator)

        val request = CreateChatRoomRequest(
            roomType = RoomType.GROUP,
            name = roomName,
            memberIds = members.map { it.id!! }
        )

        return chatRoomService.createChatRoom(request, creator.id!!)
    }

    /**
     * 메시지 전송
     */
    fun sendMessage(
        roomId: Long,
        sender: Member,
        content: String = "테스트 메시지",
        messageType: MessageType = MessageType.TEXT
    ): ChatMessageResponse {
        authFixture.authenticateAs(sender)

        val request = SendMessageRequest(
            messageType = messageType,
            content = if (messageType == MessageType.TEXT) content else null,
            fileUrl = if (messageType == MessageType.IMAGE) content else null
        )

        return chatMessageService.sendMessage(roomId, sender.id!!, request)
    }

    /**
     * 여러 개의 메시지 생성 (페이징 테스트용)
     */
    fun sendMultipleMessages(
        roomId: Long,
        sender: Member,
        count: Int
    ): List<ChatMessageResponse> {
        authFixture.authenticateAs(sender)

        return (1..count).map { i ->
            val request = SendMessageRequest(
                messageType = MessageType.TEXT,
                content = "메시지 $i",
                fileUrl = null
            )
            chatMessageService.sendMessage(roomId, sender.id!!, request)
        }
    }
}
