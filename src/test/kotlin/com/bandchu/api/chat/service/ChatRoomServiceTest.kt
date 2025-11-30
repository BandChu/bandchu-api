package com.bandchu.api.chat.service

import com.bandchu.api.domain.chat.dto.CreateChatRoomRequest
import com.bandchu.api.domain.chat.dto.UpdateReadStatusRequest
import com.bandchu.api.domain.chat.model.ChatRoom
import com.bandchu.api.domain.chat.repository.ChatMessageRepository
import com.bandchu.api.domain.chat.repository.ChatRoomRepository
import com.bandchu.api.domain.chat.repository.MemberChatRoomRepository
import com.bandchu.api.domain.chat.service.ChatRoomService
import com.bandchu.api.domain.chat.table.RoomType
import com.bandchu.api.global.exception.BusinessException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ChatRoomServiceTest : FunSpec({
    
    val chatRoomRepository = mockk<ChatRoomRepository>()
    val memberChatRoomRepository = mockk<MemberChatRoomRepository>()
    val chatMessageRepository = mockk<ChatMessageRepository>()
    
    val service = ChatRoomService(
        chatRoomRepository,
        memberChatRoomRepository,
        chatMessageRepository
    )
    
    // === 채팅방 생성 테스트 ===
    
    test("DIRECT 채팅방 생성 성공") {
        // given
        val request = CreateChatRoomRequest(
            roomType = RoomType.DIRECT,
            name = null,
            memberIds = listOf(2L)
        )
        
        every { memberChatRoomRepository.findCommonDirectRoom(1L, 2L) } returns null
        every { chatRoomRepository.create(null, RoomType.DIRECT, any()) } returns 1L
        every { memberChatRoomRepository.addMember(1L, 1L, "OWNER", any()) } returns 1L
        every { memberChatRoomRepository.addMember(1L, 2L, "MEMBER", any()) } returns 2L
        
        // when
        val result = service.createChatRoom(request, 1L)
        
        // then
        result.roomId shouldBe 1L
        result.roomType shouldBe RoomType.DIRECT
        result.name shouldBe null
        
        verify(exactly = 1) { chatRoomRepository.create(null, RoomType.DIRECT, any()) }
        verify(exactly = 1) { memberChatRoomRepository.addMember(1L, 1L, "OWNER", any()) }
        verify(exactly = 1) { memberChatRoomRepository.addMember(1L, 2L, "MEMBER", any()) }
    }
    
    test("GROUP 채팅방 생성 성공") {
        // given
        val request = CreateChatRoomRequest(
            roomType = RoomType.GROUP,
            name = "알고리즘 스터디",
            memberIds = listOf(2L, 3L)
        )
        
        every { chatRoomRepository.create("알고리즘 스터디", RoomType.GROUP, any()) } returns 10L
        every { memberChatRoomRepository.addMember(10L, 1L, "OWNER", any()) } returns 1L
        every { memberChatRoomRepository.addMember(10L, 2L, "MEMBER", any()) } returns 2L
        every { memberChatRoomRepository.addMember(10L, 3L, "MEMBER", any()) } returns 3L
        
        // when
        val result = service.createChatRoom(request, 1L)
        
        // then
        result.roomId shouldBe 10L
        result.roomType shouldBe RoomType.GROUP
        result.name shouldBe "알고리즘 스터디"
        
        verify(exactly = 3) { memberChatRoomRepository.addMember(any(), any(), any(), any()) }
    }
    
    test("DIRECT 채팅방 중복 생성 시 예외 발생") {
        // given
        val request = CreateChatRoomRequest(
            roomType = RoomType.DIRECT,
            name = null,
            memberIds = listOf(2L)
        )
        
        every { memberChatRoomRepository.findCommonDirectRoom(1L, 2L) } returns 100L
        
        // when & then
        val exception = shouldThrow<BusinessException> {
            service.createChatRoom(request, 1L)
        }
        
        exception.errorCode.name shouldBe "CHATROOM_DUPLICATE_DIRECT"
    }
    
    test("DIRECT 채팅방에 memberIds가 2명 이상이면 예외 발생") {
        // given
        val request = CreateChatRoomRequest(
            roomType = RoomType.DIRECT,
            name = null,
            memberIds = listOf(2L, 3L) // 2명 이상
        )
        
        // when & then
        shouldThrow<BusinessException> {
            service.createChatRoom(request, 1L)
        }
    }
    
    // === 채팅방 목록 조회 테스트 ===
    
    test("채팅방 목록 조회 - 빈 목록") {
        // given
        every { memberChatRoomRepository.findRoomIdsByMemberId(1L) } returns emptyList()
        
        // when
        val result = service.getChatRoomList(1L)
        
        // then
        result.rooms.size shouldBe 0
    }
    
    test("채팅방 목록 조회 - 메시지가 없는 경우") {
        // given
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val chatRoom = ChatRoom(1L, "테스트방", RoomType.GROUP, now)
        
        every { memberChatRoomRepository.findRoomIdsByMemberId(1L) } returns listOf(1L)
        every { chatRoomRepository.findByIds(listOf(1L)) } returns listOf(chatRoom)
        every { chatMessageRepository.findLastMessageByRoomId(1L) } returns null
        every { memberChatRoomRepository.findLastReadMessageId(1L, 1L) } returns null
        every { chatMessageRepository.countAllMessages(1L, 1L) } returns 0
        
        // when
        val result = service.getChatRoomList(1L)
        
        // then
        result.rooms.size shouldBe 1
        result.rooms[0].roomId shouldBe 1L
        result.rooms[0].lastMessage shouldBe null
        result.rooms[0].unreadCount shouldBe 0
    }
    
    // === 읽음 처리 테스트 ===
    
    test("읽음 처리 성공") {
        // given
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val chatRoom = ChatRoom(1L, null, RoomType.DIRECT, now)
        val request = UpdateReadStatusRequest(lastReadMessageId = 10L)
        
        every { chatRoomRepository.findById(1L) } returns chatRoom
        every { memberChatRoomRepository.findMemberIdsByRoomId(1L) } returns listOf(1L, 2L)
        every { memberChatRoomRepository.updateLastReadMessageId(1L, 1L, 10L) } returns 1
        
        // when
        val result = service.updateReadStatus(1L, request, 1L)
        
        // then
        result.roomId shouldBe 1L
        result.lastReadMessageId shouldBe 10L
        
        verify(exactly = 1) { memberChatRoomRepository.updateLastReadMessageId(1L, 1L, 10L) }
    }
    
    test("읽음 처리 - 채팅방이 없으면 예외 발생") {
        // given
        val request = UpdateReadStatusRequest(lastReadMessageId = 10L)
        
        every { chatRoomRepository.findById(999L) } returns null
        
        // when & then
        val exception = shouldThrow<BusinessException> {
            service.updateReadStatus(999L, request, 1L)
        }
        
        exception.errorCode.name shouldBe "CHATROOM_NOT_FOUND"
    }
    
    test("읽음 처리 - 참여자가 아니면 예외 발생") {
        // given
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val chatRoom = ChatRoom(1L, null, RoomType.DIRECT, now)
        val request = UpdateReadStatusRequest(lastReadMessageId = 10L)
        
        every { chatRoomRepository.findById(1L) } returns chatRoom
        every { memberChatRoomRepository.findMemberIdsByRoomId(1L) } returns listOf(2L, 3L) // 1L 없음
        
        // when & then
        val exception = shouldThrow<BusinessException> {
            service.updateReadStatus(1L, request, 1L)
        }
        
        exception.errorCode.name shouldBe "NOT_CHATROOM_MEMBER"
    }
})
