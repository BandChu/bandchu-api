package com.bandchu.api.domain.chat.repository

import com.bandchu.api.domain.chat.table.ChatRoomTable
import com.bandchu.api.domain.chat.table.MemberChatRoomTable
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
class MemberChatRoomRepositoryTest(
    private val memberChatRoomRepository: MemberChatRoomRepository,
    private val authFixture: AuthFixture
) : DescribeSpec() {

    private lateinit var member1: Member
    private lateinit var member2: Member
    private lateinit var member3: Member

    init {
        beforeSpec {
            val suffix = System.currentTimeMillis()

            member1 = authFixture.createMember(
                AuthFixture.AuthCredentials(
                    "member1-$suffix@test.com",
                    "pass",
                    "Member1",
                    Role.FAN
                )
            )
            member2 = authFixture.createMember(
                AuthFixture.AuthCredentials(
                    "member2-$suffix@test.com",
                    "pass",
                    "Member2",
                    Role.FAN
                )
            )
            member3 = authFixture.createMember(
                AuthFixture.AuthCredentials(
                    "member3-$suffix@test.com",
                    "pass",
                    "Member3",
                    Role.FAN
                )
            )
        }

        describe("addMember - 채팅방에 회원 추가") {

            context("회원을 채팅방에 추가") {
                it("MemberChatRoom ID가 반환된다") {
                    val roomId = transaction {
                        ChatRoomTable.insert {
                            it[name] = "테스트방"
                            it[roomType] = RoomType.GROUP
                            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        } get ChatRoomTable.id
                    }

                    val memberChatRoomId = memberChatRoomRepository.addMember(
                        roomId = roomId,
                        memberId = member1.id!!,
                        role = "OWNER",
                        joinedAt = OffsetDateTime.now(ZoneOffset.UTC)
                    )

                    (memberChatRoomId > 0L) shouldBe true
                }
            }
        }

        describe("findRoomIdsByMemberId - 회원이 속한 채팅방 ID 조회") {

            context("여러 채팅방에 속한 회원") {
                it("모든 채팅방 ID를 반환한다") {
                    val roomIds = transaction {
                        // 채팅방 3개 생성
                        val room1 = ChatRoomTable.insert {
                            it[name] = "방1"
                            it[roomType] = RoomType.GROUP
                            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        } get ChatRoomTable.id

                        val room2 = ChatRoomTable.insert {
                            it[name] = "방2"
                            it[roomType] = RoomType.GROUP
                            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        } get ChatRoomTable.id

                        val room3 = ChatRoomTable.insert {
                            it[name] = "방3"
                            it[roomType] = RoomType.GROUP
                            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        } get ChatRoomTable.id

                        // member1을 모든 방에 추가
                        listOf(room1, room2, room3).forEach { roomId ->
                            MemberChatRoomTable.insert {
                                it[MemberChatRoomTable.roomId] = roomId
                                it[memberId] = member1.id!!
                                it[role] = "MEMBER"
                                it[lastReadMessageId] = null
                                it[joinedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                            }
                        }

                        listOf(room1, room2, room3)
                    }

                    val result = memberChatRoomRepository.findRoomIdsByMemberId(member1.id!!)

                    result.size shouldBe 3
                    result.containsAll(roomIds) shouldBe true
                }
            }

            context("채팅방에 속하지 않은 회원") {
                it("빈 리스트를 반환한다") {
                    val result = memberChatRoomRepository.findRoomIdsByMemberId(member3.id!!)

                    result.size shouldBe 0
                }
            }
        }

        describe("findMemberIdsByRoomId - 채팅방의 모든 참여자 ID 조회") {

            context("여러 회원이 속한 채팅방") {
                it("모든 참여자 ID를 반환한다") {
                    val roomId = transaction {
                        val id = ChatRoomTable.insert {
                            it[name] = "그룹방"
                            it[roomType] = RoomType.GROUP
                            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        } get ChatRoomTable.id

                        // 3명 추가
                        listOf(member1, member2, member3).forEach { member ->
                            MemberChatRoomTable.insert {
                                it[MemberChatRoomTable.roomId] = id
                                it[memberId] = member.id!!
                                it[role] = "MEMBER"
                                it[lastReadMessageId] = null
                                it[joinedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                            }
                        }

                        id
                    }

                    val result = memberChatRoomRepository.findMemberIdsByRoomId(roomId)

                    result.size shouldBe 3
                    result.contains(member1.id!!) shouldBe true
                    result.contains(member2.id!!) shouldBe true
                    result.contains(member3.id!!) shouldBe true
                }
            }
        }

        describe("updateLastReadMessageId - 읽음 상태 업데이트") {

            context("정상적인 업데이트 요청") {
                it("lastReadMessageId가 업데이트되고 영향받은 행 수를 반환한다") {
                    val roomId = transaction {
                        val id = ChatRoomTable.insert {
                            it[name] = null
                            it[roomType] = RoomType.DIRECT
                            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        } get ChatRoomTable.id

                        MemberChatRoomTable.insert {
                            it[MemberChatRoomTable.roomId] = id
                            it[memberId] = member1.id!!
                            it[role] = "OWNER"
                            it[lastReadMessageId] = null
                            it[joinedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        }

                        id
                    }

                    val updatedRows = memberChatRoomRepository.updateLastReadMessageId(
                        roomId = roomId,
                        memberId = member1.id!!,
                        messageId = 100L
                    )

                    updatedRows shouldBe 1

                    // 실제로 업데이트되었는지 확인
                    val lastRead = memberChatRoomRepository.findLastReadMessageId(roomId, member1.id!!)
                    lastRead shouldBe 100L
                }
            }
        }

        describe("findLastReadMessageId - 마지막 읽은 메시지 ID 조회") {

            context("읽은 메시지가 있는 경우") {
                it("lastReadMessageId를 반환한다") {
                    val roomId = transaction {
                        val id = ChatRoomTable.insert {
                            it[name] = null
                            it[roomType] = RoomType.DIRECT
                            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        } get ChatRoomTable.id

                        MemberChatRoomTable.insert {
                            it[MemberChatRoomTable.roomId] = id
                            it[memberId] = member1.id!!
                            it[role] = "MEMBER"
                            it[lastReadMessageId] = 50L
                            it[joinedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        }

                        id
                    }

                    val result = memberChatRoomRepository.findLastReadMessageId(roomId, member1.id!!)

                    result shouldBe 50L
                }
            }

            context("읽은 메시지가 없는 경우") {
                it("null을 반환한다") {
                    val roomId = transaction {
                        val id = ChatRoomTable.insert {
                            it[name] = null
                            it[roomType] = RoomType.DIRECT
                            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        } get ChatRoomTable.id

                        MemberChatRoomTable.insert {
                            it[MemberChatRoomTable.roomId] = id
                            it[memberId] = member1.id!!
                            it[role] = "MEMBER"
                            it[lastReadMessageId] = null
                            it[joinedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        }

                        id
                    }

                    val result = memberChatRoomRepository.findLastReadMessageId(roomId, member1.id!!)

                    result shouldBe null
                }
            }
        }

        describe("findCommonDirectRoom - 두 사용자 간 공통 1:1 채팅방 찾기") {

            context("두 사용자 간 DIRECT 채팅방이 존재하는 경우") {
                it("채팅방 ID를 반환한다") {
                    val directRoomId = transaction {
                        val id = ChatRoomTable.insert {
                            it[name] = null
                            it[roomType] = RoomType.DIRECT
                            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        } get ChatRoomTable.id

                        // member1과 member2를 추가
                        MemberChatRoomTable.insert {
                            it[MemberChatRoomTable.roomId] = id
                            it[memberId] = member1.id!!
                            it[role] = "OWNER"
                            it[lastReadMessageId] = null
                            it[joinedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        }

                        MemberChatRoomTable.insert {
                            it[MemberChatRoomTable.roomId] = id
                            it[memberId] = member2.id!!
                            it[role] = "MEMBER"
                            it[lastReadMessageId] = null
                            it[joinedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        }

                        id
                    }

                    val result = memberChatRoomRepository.findCommonDirectRoom(member1.id!!, member2.id!!)

                    result shouldBe directRoomId
                }
            }

            context("두 사용자 간 DIRECT 채팅방이 없는 경우") {
                it("null을 반환한다") {
                    val result = memberChatRoomRepository.findCommonDirectRoom(member1.id!!, member3.id!!)

                    result shouldBe null
                }
            }

            context("GROUP 채팅방만 존재하는 경우") {
                it("null을 반환한다 (DIRECT만 찾음)") {
                    transaction {
                        val groupRoomId = ChatRoomTable.insert {
                            it[name] = "그룹방"
                            it[roomType] = RoomType.GROUP
                            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        } get ChatRoomTable.id

                        MemberChatRoomTable.insert {
                            it[MemberChatRoomTable.roomId] = groupRoomId
                            it[memberId] = member1.id!!
                            it[role] = "OWNER"
                            it[lastReadMessageId] = null
                            it[joinedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        }

                        MemberChatRoomTable.insert {
                            it[MemberChatRoomTable.roomId] = groupRoomId
                            it[memberId] = member2.id!!
                            it[role] = "MEMBER"
                            it[lastReadMessageId] = null
                            it[joinedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        }
                    }

                    val result = memberChatRoomRepository.findCommonDirectRoom(member1.id!!, member2.id!!)

                    result shouldBe null
                }
            }
        }

        describe("findMembersByRoomId - 채팅방의 멤버 정보 조회") {

            context("여러 회원이 속한 채팅방") {
                it("모든 멤버의 정보를 반환한다") {
                    val roomId = transaction {
                        val id = ChatRoomTable.insert {
                            it[name] = "테스트방"
                            it[roomType] = RoomType.GROUP
                            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        } get ChatRoomTable.id

                        listOf(member1, member2).forEach { member ->
                            MemberChatRoomTable.insert {
                                it[MemberChatRoomTable.roomId] = id
                                it[memberId] = member.id!!
                                it[role] = "MEMBER"
                                it[lastReadMessageId] = null
                                it[joinedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                            }
                        }

                        id
                    }

                    val result = memberChatRoomRepository.findMembersByRoomId(roomId)

                    result.size shouldBe 2
                    result.any { it.userId == member1.id!! } shouldBe true
                    result.any { it.userId == member2.id!! } shouldBe true
                    result.any { it.username == member1.nickname } shouldBe true
                    result.any { it.username == member2.nickname } shouldBe true
                }
            }

            context("멤버가 없는 채팅방") {
                it("빈 리스트를 반환한다") {
                    val emptyRoomId = transaction {
                        ChatRoomTable.insert {
                            it[name] = "빈 방"
                            it[roomType] = RoomType.GROUP
                            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                        } get ChatRoomTable.id
                    }

                    val result = memberChatRoomRepository.findMembersByRoomId(emptyRoomId)

                    result.size shouldBe 0
                }
            }
        }
    }
}
