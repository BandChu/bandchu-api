package com.bandchu.api.chat.service

import com.bandchu.api.domain.chat.table.MessageType
import com.bandchu.api.chat.dto.ChatMessageResponse
import com.bandchu.api.chat.dto.SendMessageRequest
import com.bandchu.api.domain.chat.repository.ChatMessageRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ChatMessageServiceTest : FunSpec({
    val chatMessageRepository = mockk<ChatMessageRepository>()
    val simpMessagingTemplate = mockk<SimpMessagingTemplate>(relaxed = true)

    val service = ChatMessageService(chatMessageRepository, simpMessagingTemplate)

    test("채팅방 참여자가 아닐 경우 FORBIDDEN 예외 발생") {
        // given
        every { chatMessageRepository.isRoomMember(1L, 100L) } returns false

        val req = SendMessageRequest(MessageType.TEXT, "안녕하세요", null)

        // when & then
        shouldThrow<ResponseStatusException> {
            service.sendMessage(1L, 100L, req)
        }.statusCode shouldBe HttpStatus.FORBIDDEN
    }

    test("메시지 저장 후 WebSocket으로 브로드캐스트 되어야 함") {
        // given
        every { chatMessageRepository.isRoomMember(1L, 100L) } returns true

        val req = SendMessageRequest(MessageType.TEXT, "안녕하세요", null)
        val savedMessage = ChatMessageResponse(
            messageId = 1L,
            roomId = 1L,
            senderId = 100L,
            messageType = MessageType.TEXT,
            content = "메시지 내용",
            fileUrl = null,
            createdAt = OffsetDateTime.now(ZoneOffset.UTC)
        )

        every { chatMessageRepository.saveMessage(1L, 100L, req) } returns savedMessage
        every { simpMessagingTemplate.convertAndSend(any<String>(), any<Any>()) } returns Unit

        // when
        val result = service.sendMessage(1L, 100L, req)

        // then
        result shouldBe savedMessage

        verify(exactly = 1) {
            chatMessageRepository.saveMessage(1L, 100L, req)
        }

        verify(exactly = 1) {
            simpMessagingTemplate.convertAndSend(
                "/topic/chatroom.1",
                savedMessage
            )
        }
    }
})