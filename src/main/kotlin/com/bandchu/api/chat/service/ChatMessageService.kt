package com.bandchu.api.chat.service

import com.bandchu.api.chat.dto.ChatMessageResponse
import com.bandchu.api.chat.dto.SendMessageRequest
import com.bandchu.api.domain.chat.repository.ChatMessageRepository
import org.springframework.http.HttpStatus
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val simpMessagingTemplate: SimpMessagingTemplate //웹소켓 구성파일 필요
) {
    fun sendMessage(roomId: Long, senderId: Long, req: SendMessageRequest): ChatMessageResponse {
        //1. 채팅방 참여자 검증 로직
        if (!chatMessageRepository.isRoomMember(roomId, senderId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "해당 채팅방의 참여자가 아닙니다.")
        }

        //2. 메시지 저장 및 DTO 반환
        val message = chatMessageRepository.saveMessage(roomId, senderId, req)

        //3. 저장한 메시지를 브로드캐스트
        simpMessagingTemplate.convertAndSend(
            "/topic/chatroom.$roomId",
            message
        )

        return message
    }
}