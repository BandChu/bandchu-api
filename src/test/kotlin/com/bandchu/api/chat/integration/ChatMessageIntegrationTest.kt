package com.bandchu.api.chat.integration

import com.bandchu.api.domain.chat.dto.CreateChatRoomRequest
import com.bandchu.api.domain.chat.dto.SendMessageRequest
import com.bandchu.api.domain.chat.service.ChatMessageService
import com.bandchu.api.domain.chat.service.ChatRoomService
import com.bandchu.api.domain.chat.table.MessageType
import com.bandchu.api.domain.chat.table.RoomType
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ChatMessageIntegrationTest
@Autowired
constructor(
        private val chatMessageService: ChatMessageService,
        private val chatRoomService: ChatRoomService
) {

    @MockBean private lateinit var simpMessagingTemplate: SimpMessagingTemplate

    @Test
    @DisplayName("메시지 전송 성공 및 웹소켓 발행 검증")
    fun sendMessageSuccess() {
        // given
        val senderId = 1L
        val room = createTestChatRoom(listOf(senderId, 2L))

        val request =
                SendMessageRequest(
                        messageType = MessageType.TEXT,
                        content = "안녕하세요 테스트 메시지입니다.",
                        fileUrl = null
                )

        // when
        val response = chatMessageService.sendMessage(room.roomId, senderId, request)

        // then
        assertNotNull(response.messageId)
        assertEquals(request.content, response.content)
        assertEquals(senderId, response.senderId)
        assertEquals(room.roomId, response.roomId)

        // 웹소켓 호출 검증 (Standard Mockito 사용)
        verify(simpMessagingTemplate)
                .convertAndSend(eq("/topic/chatroom.${room.roomId}"), any(Object::class.java))
    }

    @Test
    @DisplayName("참여자가 아닌 경우 메시지 전송 실패")
    fun sendMessageFail_NotMember() {
        // given
        val memberId = 1L
        val outsiderId = 999L
        val room = createTestChatRoom(listOf(memberId))

        val request =
                SendMessageRequest(
                        messageType = MessageType.TEXT,
                        content = "침입 시도",
                        fileUrl = null
                )

        // when & then
        val exception =
                assertThrows<BusinessException> {
                    chatMessageService.sendMessage(room.roomId, outsiderId, request)
                }
        assertEquals(ErrorCode.NOT_CHATROOM_MEMBER, exception.errorCode)
    }

    @Test
    @DisplayName("메시지 목록 조회 및 페이징(Cursor) 테스트")
    fun fetchMessagesWithPaging() {
        // given
        val senderId = 1L
        val room = createTestChatRoom(listOf(senderId))

        // 메시지 20개 생성
        for (i in 1..20) {
            val req = SendMessageRequest(MessageType.TEXT, "메시지 $i", null)
            chatMessageService.sendMessage(room.roomId, senderId, req)
        }

        // when 1: 첫 번째 페이지 조회 (최신 10개)
        val firstPage = chatMessageService.fetchMessages(room.roomId, null, 10)

        // then 1
        assertEquals(10, firstPage.messages.size)
        assertEquals("메시지 20", firstPage.messages.last().content)
        assertNotNull(firstPage.nextCursor)

        // when 2: 두 번째 페이지 조회
        val secondPage = chatMessageService.fetchMessages(room.roomId, firstPage.nextCursor, 10)

        // then 2
        assertEquals(10, secondPage.messages.size)
        assertEquals("메시지 10", secondPage.messages.last().content)
        assertEquals("메시지 1", secondPage.messages.first().content)

        // when 3: 더 이상 조회할 메시지가 없는 경우
        val emptyPage = chatMessageService.fetchMessages(room.roomId, 0L, 10)
        assertTrue(emptyPage.messages.isEmpty())
        assertNull(emptyPage.nextCursor)
    }

    // --- Helper Method ---
    private fun createTestChatRoom(
            memberIds: List<Long>
    ): com.bandchu.api.domain.chat.dto.CreateChatRoomResponse {
        val request =
                CreateChatRoomRequest(
                        roomType = RoomType.GROUP,
                        name = "테스트방",
                        memberIds = memberIds
                )
        return chatRoomService.createChatRoom(request, memberIds.first())
    }
}
