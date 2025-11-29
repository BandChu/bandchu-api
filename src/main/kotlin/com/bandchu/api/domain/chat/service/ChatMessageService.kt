package com.bandchu.api.domain.chat.service

import com.bandchu.api.domain.chat.dto.ChatMessageResponse
import com.bandchu.api.domain.chat.dto.MessagePageResponse
import com.bandchu.api.domain.chat.dto.SendMessageRequest
import com.bandchu.api.domain.chat.repository.ChatMessageRepository
import com.bandchu.api.domain.chat.repository.ChatRoomRepository
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val simpMessagingTemplate: SimpMessagingTemplate, //웹소켓 구성파일 필요
    private val chatRoomRepository: ChatRoomRepository
) {
    fun sendMessage(roomId: Long, senderId: Long, req: SendMessageRequest): ChatMessageResponse {
        //1. 채팅방 참여자, 채팅방 존재 여부 검증 로직
        if (!chatMessageRepository.isRoomMember(roomId, senderId)) {
            throw BusinessException(ErrorCode.NOT_CHATROOM_MEMBER)
        }
        val chatRoom = chatRoomRepository.findById(roomId)
            ?: throw BusinessException(ErrorCode.CHATROOM_NOT_FOUND)

        //2. 메시지 저장 및 DTO 반환
        val message = chatMessageRepository.saveMessage(roomId, senderId, req)

        //3. 저장한 메시지를 브로드캐스트
        simpMessagingTemplate.convertAndSend(
            "/topic/chatroom.$roomId",
            message
        )

        return message
    }

    fun fetchMessages(roomId: Long, cursor: Long?, size: Int): MessagePageResponse {
        //레포지토리에서 List<SendMessageResponse>를 가져옴 -> 시간 순으로 정렬되어 있음
        val messages = chatMessageRepository.fetchMessages(roomId, cursor, size)

        //nextCursor 값 지정
        val nextCursor = if (messages.isNotEmpty()) {
            messages.first().messageId // -1을 제거해야 통합 테스트 가능했음(ChatMessageIntegrationTest)
        } else {
            null
        }

        return MessagePageResponse(
            messages = messages,
            nextCursor = nextCursor
        )
    }
}