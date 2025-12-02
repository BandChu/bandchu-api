package com.bandchu.api.chat.integration

import com.bandchu.api.domain.chat.dto.CreateChatRoomRequest
import com.bandchu.api.domain.chat.dto.UpdateReadStatusRequest
import com.bandchu.api.domain.chat.service.ChatRoomService
import com.bandchu.api.domain.chat.table.RoomType
import com.bandchu.api.global.exception.BusinessException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ChatRoomIntegrationTest @Autowired constructor(
    private val chatRoomService: ChatRoomService
) {
    
    @Test
    fun `DIRECT 채팅방 생성 성공`() {
        // given
        val request = CreateChatRoomRequest(
            roomType = RoomType.DIRECT,
            name = null,
            memberIds = listOf(2L)
        )
        
        // when
        val result = chatRoomService.createChatRoom(request, 1L)
        
        // then
        assertNotNull(result.roomId)
        assertEquals(RoomType.DIRECT, result.roomType)
        assertNull(result.name)
    }
    
    @Test
    fun `GROUP 채팅방 생성 성공`() {
        // given
        val request = CreateChatRoomRequest(
            roomType = RoomType.GROUP,
            name = "알고리즘 스터디",
            memberIds = listOf(2L, 3L)
        )
        
        // when
        val result = chatRoomService.createChatRoom(request, 1L)
        
        // then
        assertNotNull(result.roomId)
        assertEquals(RoomType.GROUP, result.roomType)
        assertEquals("알고리즘 스터디", result.name)
    }
    
    @Test
    fun `DIRECT 채팅방 중복 생성 시 예외 발생`() {
        // given
        val request = CreateChatRoomRequest(
            roomType = RoomType.DIRECT,
            name = null,
            memberIds = listOf(2L)
        )
        
        chatRoomService.createChatRoom(request, 1L)
        
        // when & then
        assertThrows<BusinessException> {
            chatRoomService.createChatRoom(request, 1L)
        }
    }
    
    @Test
    fun `채팅방 목록 조회 성공`() {
        // given
        val request = CreateChatRoomRequest(
            roomType = RoomType.GROUP,
            name = "테스트방",
            memberIds = listOf(2L)
        )
        
        chatRoomService.createChatRoom(request, 1L)
        
        // when
        val result = chatRoomService.getChatRoomList(1L)
        
        // then
        assertTrue(result.rooms.isNotEmpty())
    }
    
    @Test
    fun `읽음 처리 성공`() {
        // given
        val createRequest = CreateChatRoomRequest(
            roomType = RoomType.DIRECT,
            name = null,
            memberIds = listOf(2L)
        )
        
        val created = chatRoomService.createChatRoom(createRequest, 1L)
        val updateRequest = UpdateReadStatusRequest(lastReadMessageId = 1L)
        
        // when
        val result = chatRoomService.updateReadStatus(created.roomId, updateRequest, 1L)
        
        // then
        assertEquals(created.roomId, result.roomId)
        assertEquals(1L, result.lastReadMessageId)
    }
    
    @Test
    fun `참여하지 않은 채팅방 읽음 처리 시 예외 발생`() {
        // given
        val createRequest = CreateChatRoomRequest(
            roomType = RoomType.DIRECT,
            name = null,
            memberIds = listOf(2L)
        )
        
        val created = chatRoomService.createChatRoom(createRequest, 1L)
        val updateRequest = UpdateReadStatusRequest(lastReadMessageId = 1L)
        
        // when & then
        assertThrows<BusinessException> {
            chatRoomService.updateReadStatus(created.roomId, updateRequest, 999L)
        }
    }
}
