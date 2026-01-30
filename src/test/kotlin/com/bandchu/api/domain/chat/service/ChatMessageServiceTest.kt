package com.bandchu.api.domain.chat.service

import com.bandchu.api.domain.chat.dto.ChatMessageResponse
import com.bandchu.api.domain.chat.dto.SendMessageRequest
import com.bandchu.api.domain.chat.model.ChatRoom
import com.bandchu.api.domain.chat.repository.ChatMessageRepository
import com.bandchu.api.domain.chat.repository.ChatRoomRepository
import com.bandchu.api.domain.chat.table.MessageType
import com.bandchu.api.domain.chat.table.RoomType
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.time.OffsetDateTime

class ChatMessageServiceTest : DescribeSpec() {

    private val chatMessageRepository = mockk<ChatMessageRepository>()
    private val simpMessagingTemplate = mockk<SimpMessagingTemplate>(relaxed = true)
    private val chatRoomRepository = mockk<ChatRoomRepository>()

    private val chatMessageService =
        ChatMessageService(chatMessageRepository, simpMessagingTemplate, chatRoomRepository)

    init {
        afterEach {
            clearMocks(chatMessageRepository, simpMessagingTemplate, chatRoomRepository)
        }

        describe("sendMessage - 메시지 전송") {
            val roomId = 100L
            val senderId = 1L

            context("정상적인 메시지 전송 시") {
                it("TEXT 메시지가 저장되고 WebSocket으로 브로드캐스트된다") {
                    val request = SendMessageRequest(
                        messageType = MessageType.TEXT,
                        content = "안녕하세요",
                        fileUrl = null
                    )

                    val savedMessage = ChatMessageResponse(
                        messageId = 1L,
                        roomId = roomId,
                        senderId = senderId,
                        messageType = MessageType.TEXT,
                        content = "안녕하세요",
                        fileUrl = null,
                        createdAt = OffsetDateTime.now()
                    )

                    val chatRoom = ChatRoom(roomId, null, RoomType.DIRECT, OffsetDateTime.now())

                    every { chatMessageRepository.isRoomMember(roomId, senderId) } returns true
                    every { chatRoomRepository.findById(roomId) } returns chatRoom
                    every { chatMessageRepository.saveMessage(roomId, senderId, request) } returns savedMessage

                    val result = chatMessageService.sendMessage(roomId, senderId, request)

                    result.messageId shouldBe 1L
                    result.content shouldBe "안녕하세요"
                    result.messageType shouldBe MessageType.TEXT

                    verify(exactly = 1) {
                        simpMessagingTemplate.convertAndSend("/topic/chatroom.$roomId", savedMessage)
                    }
                }

                it("IMAGE 메시지가 저장되고 WebSocket으로 브로드캐스트된다") {
                    val request = SendMessageRequest(
                        messageType = MessageType.IMAGE,
                        content = null,
                        fileUrl = "https://example.com/image.jpg"
                    )

                    val savedMessage = ChatMessageResponse(
                        messageId = 2L,
                        roomId = roomId,
                        senderId = senderId,
                        messageType = MessageType.IMAGE,
                        content = null,
                        fileUrl = "https://example.com/image.jpg",
                        createdAt = OffsetDateTime.now()
                    )

                    val chatRoom = ChatRoom(roomId, null, RoomType.DIRECT, OffsetDateTime.now())

                    every { chatMessageRepository.isRoomMember(roomId, senderId) } returns true
                    every { chatRoomRepository.findById(roomId) } returns chatRoom
                    every { chatMessageRepository.saveMessage(roomId, senderId, request) } returns savedMessage

                    val result = chatMessageService.sendMessage(roomId, senderId, request)

                    result.messageId shouldBe 2L
                    result.fileUrl shouldBe "https://example.com/image.jpg"
                    result.messageType shouldBe MessageType.IMAGE

                    verify(exactly = 1) {
                        simpMessagingTemplate.convertAndSend("/topic/chatroom.$roomId", savedMessage)
                    }
                }
            }

            context("채팅방 참여자가 아닌 경우") {
                it("NOT_CHATROOM_MEMBER 예외를 던진다") {
                    val request = SendMessageRequest(
                        messageType = MessageType.TEXT,
                        content = "안녕하세요",
                        fileUrl = null
                    )

                    every { chatMessageRepository.isRoomMember(roomId, senderId) } returns false

                    shouldThrow<BusinessException> {
                        chatMessageService.sendMessage(roomId, senderId, request)
                    }.errorCode shouldBe ErrorCode.NOT_CHATROOM_MEMBER

                    verify(exactly = 0) {
                        chatMessageRepository.saveMessage(any(), any(), any())
                    }
                }
            }

            context("존재하지 않는 채팅방인 경우") {
                it("CHATROOM_NOT_FOUND 예외를 던진다") {
                    val request = SendMessageRequest(
                        messageType = MessageType.TEXT,
                        content = "안녕하세요",
                        fileUrl = null
                    )

                    every { chatMessageRepository.isRoomMember(roomId, senderId) } returns true
                    every { chatRoomRepository.findById(roomId) } returns null

                    shouldThrow<BusinessException> {
                        chatMessageService.sendMessage(roomId, senderId, request)
                    }.errorCode shouldBe ErrorCode.CHATROOM_NOT_FOUND

                    verify(exactly = 0) {
                        chatMessageRepository.saveMessage(any(), any(), any())
                    }
                }
            }
        }

        describe("fetchMessages - 메시지 조회") {
            val roomId = 100L

            context("커서가 없는 경우 (첫 페이지)") {
                it("최신 메시지부터 size개만큼 조회하고 nextCursor를 반환한다") {
                    val messages = listOf(
                        ChatMessageResponse(1L, roomId, 1L, MessageType.TEXT, "첫 메시지", null, OffsetDateTime.now()),
                        ChatMessageResponse(2L, roomId, 2L, MessageType.TEXT, "두 번째 메시지", null, OffsetDateTime.now()),
                        ChatMessageResponse(3L, roomId, 1L, MessageType.TEXT, "세 번째 메시지", null, OffsetDateTime.now())
                    )

                    every { chatMessageRepository.fetchMessages(roomId, null, 30) } returns messages

                    val result = chatMessageService.fetchMessages(roomId, null, 30)

                    result.messages.size shouldBe 3
                    result.nextCursor shouldBe 1L // 첫 번째 메시지 ID
                }
            }

            context("커서가 있는 경우 (다음 페이지)") {
                it("커서 이전의 메시지를 조회한다") {
                    val cursor = 10L
                    val messages = listOf(
                        ChatMessageResponse(7L, roomId, 1L, MessageType.TEXT, "이전 메시지 1", null, OffsetDateTime.now()),
                        ChatMessageResponse(8L, roomId, 2L, MessageType.TEXT, "이전 메시지 2", null, OffsetDateTime.now()),
                        ChatMessageResponse(9L, roomId, 1L, MessageType.TEXT, "이전 메시지 3", null, OffsetDateTime.now())
                    )

                    every { chatMessageRepository.fetchMessages(roomId, cursor, 30) } returns messages

                    val result = chatMessageService.fetchMessages(roomId, cursor, 30)

                    result.messages.size shouldBe 3
                    result.nextCursor shouldBe 7L
                }
            }

            context("메시지가 없는 경우") {
                it("빈 리스트와 null nextCursor를 반환한다") {
                    every { chatMessageRepository.fetchMessages(roomId, null, 30) } returns emptyList()

                    val result = chatMessageService.fetchMessages(roomId, null, 30)

                    result.messages.size shouldBe 0
                    result.nextCursor shouldBe null
                }
            }

            context("커스텀 size로 조회") {
                it("지정한 size만큼만 조회한다") {
                    val customSize = 10
                    val messages = (1..10).map {
                        ChatMessageResponse(
                            it.toLong(),
                            roomId,
                            1L,
                            MessageType.TEXT,
                            "메시지 $it",
                            null,
                            OffsetDateTime.now()
                        )
                    }

                    every { chatMessageRepository.fetchMessages(roomId, null, customSize) } returns messages

                    val result = chatMessageService.fetchMessages(roomId, null, customSize)

                    result.messages.size shouldBe 10
                    verify(exactly = 1) {
                        chatMessageRepository.fetchMessages(roomId, null, customSize)
                    }
                }
            }
        }
    }
}
