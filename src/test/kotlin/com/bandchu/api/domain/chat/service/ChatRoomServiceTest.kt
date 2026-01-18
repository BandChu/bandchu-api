package com.bandchu.api.domain.chat.service

import com.bandchu.api.domain.chat.dto.CreateChatRoomRequest
import com.bandchu.api.domain.chat.dto.UpdateReadStatusRequest
import com.bandchu.api.domain.chat.model.ChatRoom
import com.bandchu.api.domain.chat.repository.ChatMessageRepository
import com.bandchu.api.domain.chat.repository.ChatRoomRepository
import com.bandchu.api.domain.chat.repository.MemberChatRoomRepository
import com.bandchu.api.domain.chat.table.RoomType
import com.bandchu.api.domain.member.dto.MemberInfo
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.time.OffsetDateTime

class ChatRoomServiceTest : DescribeSpec() {

    private val chatRoomRepository = mockk<ChatRoomRepository>()
    private val memberChatRoomRepository = mockk<MemberChatRoomRepository>()
    private val chatMessageRepository = mockk<ChatMessageRepository>()

    private val chatRoomService =
            ChatRoomService(chatRoomRepository, memberChatRoomRepository, chatMessageRepository)

    init {

        describe("createChatRoom - 채팅방 생성") {
            val myId = 1L
            val partnerId = 2L

            context("1:1(DIRECT) 채팅방 생성 시") {
                val request = CreateChatRoomRequest(RoomType.DIRECT, null, listOf(partnerId))

                it("이미 존재하는 방이 있다면 해당 방 정보를 반환한다 (멱등성)") {
                    val existingRoomId = 10L
                    val existingRoom =
                            ChatRoom(existingRoomId, null, RoomType.DIRECT, OffsetDateTime.now())

                    every { memberChatRoomRepository.findCommonDirectRoom(myId, partnerId) } returns
                            existingRoomId
                    every { chatRoomRepository.findById(existingRoomId) } returns existingRoom

                    val response = chatRoomService.createChatRoom(request, myId)

                    response.roomId shouldBe existingRoomId
                    response.roomType shouldBe RoomType.DIRECT
                }

                it("존재하는 방이 없다면 새로 생성하고 ID를 반환한다") {
                    val newRoomId = 20L
                    every { memberChatRoomRepository.findCommonDirectRoom(myId, partnerId) } returns
                            null
                    every { chatRoomRepository.create(any(), RoomType.DIRECT, any()) } returns
                            newRoomId
                    every { memberChatRoomRepository.addMember(any(), any(), any(), any()) } returns
                            1L

                    val response = chatRoomService.createChatRoom(request, myId)

                    response.roomId shouldBe newRoomId
                    verify(exactly = 1) { chatRoomRepository.create(null, RoomType.DIRECT, any()) }
                    verify(exactly = 2) {
                        memberChatRoomRepository.addMember(newRoomId, any(), any(), any())
                    }
                }

                it("멤버가 1명이 아니면 예외를 던진다") {
                    val invalidRequest =
                            CreateChatRoomRequest(RoomType.DIRECT, null, listOf(2L, 3L))

                    shouldThrow<BusinessException> {
                                chatRoomService.createChatRoom(invalidRequest, myId)
                            }
                            .errorCode shouldBe ErrorCode.CHATROOM_INVALID_REQUEST
                }
            }

            context("그룹(GROUP) 채팅방 생성 시") {
                it("이름이 없으면 기본 이름을 사용하여 생성한다") {
                    val members = listOf(2L, 3L)
                    val request = CreateChatRoomRequest(RoomType.GROUP, null, members)
                    val newRoomId = 30L

                    every { chatRoomRepository.create(any(), RoomType.GROUP, any()) } returns
                            newRoomId
                    every { memberChatRoomRepository.addMember(any(), any(), any(), any()) } returns
                            1L

                    val response = chatRoomService.createChatRoom(request, myId)

                    response.roomId shouldBe newRoomId
                    response.name shouldBe "Unnamed Group"
                    verify(exactly = 3) {
                        memberChatRoomRepository.addMember(newRoomId, any(), any(), any())
                    }
                }
            }
        }

        describe("getChatRoomList - 채팅방 목록 조회") {
            val myId = 1L

            context("속한 채팅방이 없을 때") {
                it("빈 리스트를 반환한다") {
                    every { memberChatRoomRepository.findRoomIdsByMemberId(myId) } returns
                            emptyList()

                    val result = chatRoomService.getChatRoomList(myId)

                    result.rooms.isEmpty() shouldBe true
                }
            }

            context("채팅방이 있을 때") {
                it("채팅방 정보와 읽지 않은 메시지 개수를 포함하여 반환한다") {
                    val roomId = 100L
                    val room = ChatRoom(roomId, "테스트방", RoomType.GROUP, OffsetDateTime.now())

                    every { memberChatRoomRepository.findRoomIdsByMemberId(myId) } returns
                            listOf(roomId)
                    every { chatRoomRepository.findByIds(listOf(roomId)) } returns listOf(room)

                    every { memberChatRoomRepository.findMembersByRoomId(roomId) } returns
                            listOf(MemberInfo(myId, "나", null), MemberInfo(2L, "친구", null))

                    every { chatMessageRepository.findLastMessageByRoomId(roomId) } returns null

                    every { memberChatRoomRepository.findLastReadMessageId(roomId, myId) } returns
                            50L
                    every { chatMessageRepository.countUnreadMessages(roomId, 50L, myId) } returns 2

                    val result = chatRoomService.getChatRoomList(myId)

                    result.rooms.size shouldBe 1
                    result.rooms[0].unreadCount shouldBe 2
                    result.rooms[0].name shouldBe "테스트방"
                }
            }
        }

        describe("updateReadStatus - 읽음 처리") {
            val myId = 1L
            val roomId = 100L
            val request = UpdateReadStatusRequest(lastReadMessageId = 200L)

            context("정상적인 요청일 때") {
                it("lastReadMessageId를 업데이트한다") {
                    val room = ChatRoom(roomId, "방", RoomType.DIRECT, OffsetDateTime.now())

                    every { chatRoomRepository.findById(roomId) } returns room
                    every { memberChatRoomRepository.findMemberIdsByRoomId(roomId) } returns
                            listOf(myId, 2L)
                    every {
                        memberChatRoomRepository.updateLastReadMessageId(roomId, myId, 200L)
                    } returns 1

                    val response = chatRoomService.updateReadStatus(roomId, request, myId)

                    response.lastReadMessageId shouldBe 200L
                    verify(exactly = 1) {
                        memberChatRoomRepository.updateLastReadMessageId(roomId, myId, 200L)
                    }
                }
            }

            context("참여하지 않은 방일 때") {
                it("NOT_CHATROOM_MEMBER 예외를 던진다") {
                    val room = ChatRoom(roomId, "방", RoomType.DIRECT, OffsetDateTime.now())

                    every { chatRoomRepository.findById(roomId) } returns room
                    every { memberChatRoomRepository.findMemberIdsByRoomId(roomId) } returns
                            listOf(2L)

                    shouldThrow<BusinessException> {
                                chatRoomService.updateReadStatus(roomId, request, myId)
                            }
                            .errorCode shouldBe ErrorCode.NOT_CHATROOM_MEMBER
                }
            }

            context("존재하지 않는 방일 때") {
                it("CHATROOM_NOT_FOUND 예외를 던진다") {
                    every { chatRoomRepository.findById(roomId) } returns null

                    shouldThrow<BusinessException> {
                                chatRoomService.updateReadStatus(roomId, request, myId)
                            }
                            .errorCode shouldBe ErrorCode.CHATROOM_NOT_FOUND
                }
            }
        }
    }
}
