package com.bandchu.api.domain.chat.repository

import com.bandchu.api.domain.chat.dto.SendMessageRequest
import com.bandchu.api.domain.chat.table.ChatMessageTable
import com.bandchu.api.domain.chat.table.ChatRoomTable
import com.bandchu.api.domain.chat.table.MemberChatRoomTable
import com.bandchu.api.domain.chat.table.MessageType
import com.bandchu.api.domain.chat.table.RoomType
import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.fixture.AuthFixture
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(com.bandchu.api.domain.chat.ChatTestConfig::class)
class ChatMessageRepositoryTest(
    private val chatMessageRepository: ChatMessageRepository,
    private val authFixture: AuthFixture
) : DescribeSpec() {

    private lateinit var sender: Member
    private lateinit var receiver: Member
    private lateinit var outsider: Member
    private var roomId: Long = 0L

    init {
        beforeSpec {
            val suffix = System.currentTimeMillis()

            sender = authFixture.createMember(
                AuthFixture.AuthCredentials(
                    "sender-$suffix@test.com",
                    "pass",
                    "Sender",
                    Role.FAN
                )
            )
            receiver = authFixture.createMember(
                AuthFixture.AuthCredentials(
                    "receiver-$suffix@test.com",
                    "pass",
                    "Receiver",
                    Role.FAN
                )
            )
            outsider = authFixture.createMember(
                AuthFixture.AuthCredentials(
                    "outsider-$suffix@test.com",
                    "pass",
                    "Outsider",
                    Role.FAN
                )
            )

            // 테스트용 채팅방 생성
            roomId = transaction {
                val id = ChatRoomTable.insert {
                    it[name] = null
                    it[roomType] = RoomType.DIRECT
                    it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                } get ChatRoomTable.id

                // 참여자 추가
                MemberChatRoomTable.insert {
                    it[MemberChatRoomTable.roomId] = id
                    it[memberId] = sender.id!!
                    it[role] = "OWNER"
                    it[lastReadMessageId] = null
                    it[joinedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                }

                MemberChatRoomTable.insert {
                    it[MemberChatRoomTable.roomId] = id
                    it[memberId] = receiver.id!!
                    it[role] = "MEMBER"
                    it[lastReadMessageId] = null
                    it[joinedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                }

                id
            }
        }

        describe("saveMessage - 메시지 저장") {

            context("TEXT 메시지 저장") {
                it("메시지가 DB에 저장되고 ChatMessageResponse가 반환된다") {
                    val request = SendMessageRequest(
                        messageType = MessageType.TEXT,
                        content = "안녕하세요",
                        fileUrl = null
                    )

                    val result = chatMessageRepository.saveMessage(roomId, sender.id!!, request)

                    result.roomId shouldBe roomId
                    result.senderId shouldBe sender.id!!
                    result.messageType shouldBe MessageType.TEXT
                    result.content shouldBe "안녕하세요"
                    result.fileUrl shouldBe null
                }
            }

            context("IMAGE 메시지 저장") {
                it("이미지 URL이 포함된 메시지가 저장된다") {
                    val request = SendMessageRequest(
                        messageType = MessageType.IMAGE,
                        content = null,
                        fileUrl = "https://example.com/image.jpg"
                    )

                    val result = chatMessageRepository.saveMessage(roomId, sender.id!!, request)

                    result.messageType shouldBe MessageType.IMAGE
                    result.content shouldBe null
                    result.fileUrl shouldBe "https://example.com/image.jpg"
                }
            }
        }

        describe("isRoomMember - 채팅방 참여자 확인") {

            context("채팅방 참여자인 경우") {
                it("true를 반환한다") {
                    val result = chatMessageRepository.isRoomMember(roomId, sender.id!!)
                    result shouldBe true
                }
            }

            context("채팅방 참여자가 아닌 경우") {
                it("false를 반환한다") {
                    val result = chatMessageRepository.isRoomMember(roomId, outsider.id!!)
                    result shouldBe false
                }
            }

            context("존재하지 않는 채팅방인 경우") {
                it("false를 반환한다") {
                    val result = chatMessageRepository.isRoomMember(99999L, sender.id!!)
                    result shouldBe false
                }
            }
        }

        describe("fetchMessages - 메시지 조회 (커서 기반 페이징)") {

            beforeEach {
                // 테스트용 메시지 10개 생성
                transaction {
                    (1..10).forEach { i ->
                        ChatMessageTable.insert {
                            it[ChatMessageTable.roomId] = roomId
                            it[senderId] = sender.id!!
                            it[messageType] = MessageType.TEXT
                            it[content] = "메시지 $i"
                            it[fileUrl] = null
                            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(i.toLong())
                        }
                    }
                }
            }

            context("cursor가 null인 경우 (첫 페이지)") {
                it("최신 메시지부터 size개만큼 조회한다") {
                    val result = chatMessageRepository.fetchMessages(roomId, null, 5)

                    result.size shouldBe 5
                    // 시간 순으로 정렬되어야 함 (오래된 것부터)
                    result.first().content shouldBe "메시지 6"
                    result.last().content shouldBe "메시지 10"
                }
            }

            context("cursor가 있는 경우 (다음 페이지)") {
                it("cursor보다 작은 ID의 메시지를 조회한다") {
                    // 먼저 첫 페이지 조회
                    val firstPage = chatMessageRepository.fetchMessages(roomId, null, 5)
                    val cursor = firstPage.first().messageId

                    // cursor 이전 메시지 조회
                    val result = chatMessageRepository.fetchMessages(roomId, cursor, 5)

                    result.size shouldBe 5
                    result.all { it.messageId < cursor } shouldBe true
                }
            }

            context("size보다 적은 메시지만 있는 경우") {
                it("존재하는 메시지만 반환한다") {
                    val result = chatMessageRepository.fetchMessages(roomId, null, 100)

                    result.size shouldBe 10
                }
            }
        }

        describe("findLastMessageByRoomId - 마지막 메시지 조회") {

            context("메시지가 있는 채팅방") {
                it("가장 최근 메시지를 반환한다") {
                    // 메시지 생성
                    transaction {
                        ChatMessageTable.insert {
                            it[ChatMessageTable.roomId] = roomId
                            it[senderId] = sender.id!!
                            it[messageType] = MessageType.TEXT
                            it[content] = "첫 메시지"
                            it[fileUrl] = null
                            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        }
                        Thread.sleep(10) // 시간차 보장
                        ChatMessageTable.insert {
                            it[ChatMessageTable.roomId] = roomId
                            it[senderId] = receiver.id!!
                            it[messageType] = MessageType.TEXT
                            it[content] = "마지막 메시지"
                            it[fileUrl] = null
                            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        }
                    }

                    val result = chatMessageRepository.findLastMessageByRoomId(roomId)

                    result?.content shouldBe "마지막 메시지"
                }
            }

            context("메시지가 없는 채팅방") {
                it("null을 반환한다") {
                    val emptyRoomId = transaction {
                        ChatRoomTable.insert {
                            it[name] = null
                            it[roomType] = RoomType.DIRECT
                            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        } get ChatRoomTable.id
                    }

                    val result = chatMessageRepository.findLastMessageByRoomId(emptyRoomId)

                    result shouldBe null
                }
            }
        }

        describe("countUnreadMessages - 읽지 않은 메시지 개수") {

            context("lastReadMessageId 이후 새 메시지가 있는 경우") {
                it("본인 메시지를 제외하고 카운트한다") {
                    val messageIds = transaction {
                        (1..5).map { i ->
                            ChatMessageTable.insert {
                                it[ChatMessageTable.roomId] = roomId
                                it[senderId] = if (i % 2 == 0) receiver.id!! else sender.id!!
                                it[messageType] = MessageType.TEXT
                                it[content] = "메시지 $i"
                                it[fileUrl] = null
                                it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(i.toLong())
                            } get ChatMessageTable.id
                        }
                    }

                    val lastReadMessageId = messageIds[1] // 2번째 메시지까지 읽음
                    val count = chatMessageRepository.countUnreadMessages(
                        roomId,
                        lastReadMessageId,
                        sender.id!! // sender가 조회하므로 sender의 메시지는 제외
                    )

                    // 3, 4, 5번 메시지 중 receiver가 보낸 메시지 (4번)만 카운트
                    count shouldBe 1
                }
            }

            context("읽지 않은 메시지가 없는 경우") {
                it("0을 반환한다") {
                    val messageId = transaction {
                        ChatMessageTable.insert {
                            it[ChatMessageTable.roomId] = roomId
                            it[senderId] = sender.id!!
                            it[messageType] = MessageType.TEXT
                            it[content] = "메시지"
                            it[fileUrl] = null
                            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        } get ChatMessageTable.id
                    }

                    val count = chatMessageRepository.countUnreadMessages(
                        roomId,
                        messageId,
                        sender.id!!
                    )

                    count shouldBe 0
                }
            }
        }

        describe("countAllMessages - 전체 메시지 개수 (본인 제외)") {

            context("여러 메시지가 있는 경우") {
                it("본인이 보낸 메시지를 제외하고 카운트한다") {
                    transaction {
                        (1..10).forEach { i ->
                            ChatMessageTable.insert {
                                it[ChatMessageTable.roomId] = roomId
                                it[senderId] = if (i % 2 == 0) sender.id!! else receiver.id!!
                                it[messageType] = MessageType.TEXT
                                it[content] = "메시지 $i"
                                it[fileUrl] = null
                                it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                            }
                        }
                    }

                    val count = chatMessageRepository.countAllMessages(roomId, sender.id!!)

                    // sender가 보내지 않은 메시지 개수 (홀수 번호 = receiver가 보낸 것)
                    count shouldBe 5
                }
            }

            context("본인만 메시지를 보낸 경우") {
                it("0을 반환한다") {
                    transaction {
                        (1..3).forEach { i ->
                            ChatMessageTable.insert {
                                it[ChatMessageTable.roomId] = roomId
                                it[senderId] = sender.id!!
                                it[messageType] = MessageType.TEXT
                                it[content] = "내 메시지 $i"
                                it[fileUrl] = null
                                it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                            }
                        }
                    }

                    val count = chatMessageRepository.countAllMessages(roomId, sender.id!!)

                    count shouldBe 0
                }
            }
        }
    }
}
