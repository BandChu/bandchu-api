package com.bandchu.api.chat.service

import com.bandchu.api.chat.persistence.table.MessageType
import com.bandchu.api.chat.dto.ChatMessageResponse
import com.bandchu.api.chat.dto.SendMessageRequest
import com.bandchu.api.chat.persistence.repository.ChatMessageRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ChatMessageServiceTest : StringSpec({

    "sendMessage should save message and return DTO" {
        // Arrange
        val repository = mockk<ChatMessageRepository>()
        val service = ChatMessageService(repository)

        val roomId = 10L
        val senderId = 1L
        val request = SendMessageRequest(
            messageType = MessageType.TEXT,
            content = "안녕하세요!",
            fileUrl = null
        )

        val expectedResponse = ChatMessageResponse(
            messageId = 100L,
            roomId = roomId,
            senderId = senderId,
            messageType = MessageType.TEXT,
            content = "안녕하세요!",
            fileUrl = null,
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        )

        // Mock repository behavior
        every { repository.isRoomMember(roomId, 1) } returns true
        every { repository.saveMessage(roomId, 1, request) } returns expectedResponse

        // Act
        val actualResponse = service.sendMessage(roomId, senderId, request)

        // Assert
        actualResponse shouldBe expectedResponse
    }

    "sendMessage should throw exception if user is not a member of the room" {
        // Arrange
        val repository = mockk<ChatMessageRepository>()
        val service = ChatMessageService(repository)

        val roomId = 10L
        val senderId = 2L
        val request = SendMessageRequest(
            messageType = MessageType.TEXT,
            content = "안녕하세요!",
            fileUrl = null
        )

        every { repository.isRoomMember(roomId, senderId) } returns false

        // Act & Assert
        val exception = shouldThrow<ResponseStatusException>{
            service.sendMessage(roomId, senderId, request)
        }

        exception.statusCode shouldBe HttpStatus.FORBIDDEN
        exception.reason shouldBe "해당 채팅방의 참여자가 아닙니다."
    }
})
