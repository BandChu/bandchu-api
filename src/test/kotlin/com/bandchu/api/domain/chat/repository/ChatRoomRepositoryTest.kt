package com.bandchu.api.domain.chat.repository

import com.bandchu.api.domain.chat.table.RoomType
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
class ChatRoomRepositoryTest(
    private val chatRoomRepository: ChatRoomRepository
) : DescribeSpec() {

    init {

        describe("create - 채팅방 생성") {

            context("DIRECT 채팅방 생성") {
                it("이름이 null인 채팅방이 생성되고 ID가 반환된다") {
                    val roomId = chatRoomRepository.create(
                        name = null,
                        roomType = RoomType.DIRECT,
                        createdAt = OffsetDateTime.now(ZoneOffset.UTC)
                    )

                    roomId shouldNotBe null
                    (roomId > 0L) shouldBe true

                    // 생성된 채팅방 확인
                    val room = chatRoomRepository.findById(roomId)
                    room shouldNotBe null
                    room?.roomType shouldBe RoomType.DIRECT
                    room?.name shouldBe null
                }
            }

            context("GROUP 채팅방 생성") {
                it("이름이 있는 채팅방이 생성된다") {
                    val roomName = "알고리즘 스터디"
                    val roomId = chatRoomRepository.create(
                        name = roomName,
                        roomType = RoomType.GROUP,
                        createdAt = OffsetDateTime.now(ZoneOffset.UTC)
                    )

                    roomId shouldNotBe null

                    // 생성된 채팅방 확인
                    val room = chatRoomRepository.findById(roomId)
                    room shouldNotBe null
                    room?.roomType shouldBe RoomType.GROUP
                    room?.name shouldBe roomName
                }
            }

            context("이름이 없는 GROUP 채팅방 생성") {
                it("이름이 null인 GROUP 채팅방이 생성된다") {
                    val roomId = chatRoomRepository.create(
                        name = null,
                        roomType = RoomType.GROUP,
                        createdAt = OffsetDateTime.now(ZoneOffset.UTC)
                    )

                    val room = chatRoomRepository.findById(roomId)
                    room shouldNotBe null
                    room?.roomType shouldBe RoomType.GROUP
                    room?.name shouldBe null
                }
            }
        }

        describe("findById - ID로 채팅방 조회") {

            context("존재하는 채팅방 ID로 조회") {
                it("채팅방 정보를 반환한다") {
                    // 먼저 채팅방 생성
                    val roomId = chatRoomRepository.create(
                        name = "테스트방",
                        roomType = RoomType.GROUP,
                        createdAt = OffsetDateTime.now(ZoneOffset.UTC)
                    )

                    val room = chatRoomRepository.findById(roomId)

                    room shouldNotBe null
                    room?.id shouldBe roomId
                    room?.name shouldBe "테스트방"
                    room?.roomType shouldBe RoomType.GROUP
                    room?.createdAt shouldNotBe null
                }
            }

            context("존재하지 않는 채팅방 ID로 조회") {
                it("null을 반환한다") {
                    val nonExistentId = 99999L
                    val room = chatRoomRepository.findById(nonExistentId)

                    room shouldBe null
                }
            }
        }

        describe("findByIds - 여러 ID로 채팅방 조회") {

            context("여러 개의 채팅방 ID로 조회") {
                it("모든 채팅방 정보를 리스트로 반환한다") {
                    // 채팅방 3개 생성
                    val room1Id = chatRoomRepository.create(
                        name = "방1",
                        roomType = RoomType.DIRECT,
                        createdAt = OffsetDateTime.now(ZoneOffset.UTC)
                    )
                    val room2Id = chatRoomRepository.create(
                        name = "방2",
                        roomType = RoomType.GROUP,
                        createdAt = OffsetDateTime.now(ZoneOffset.UTC)
                    )
                    val room3Id = chatRoomRepository.create(
                        name = "방3",
                        roomType = RoomType.GROUP,
                        createdAt = OffsetDateTime.now(ZoneOffset.UTC)
                    )

                    val rooms = chatRoomRepository.findByIds(listOf(room1Id, room2Id, room3Id))

                    rooms.size shouldBe 3
                    rooms.map { it.id }.containsAll(listOf(room1Id, room2Id, room3Id)) shouldBe true
                }
            }

            context("일부만 존재하는 ID로 조회") {
                it("존재하는 채팅방만 반환한다") {
                    val existingRoomId = chatRoomRepository.create(
                        name = "존재하는방",
                        roomType = RoomType.DIRECT,
                        createdAt = OffsetDateTime.now(ZoneOffset.UTC)
                    )

                    val rooms = chatRoomRepository.findByIds(listOf(existingRoomId, 99999L))

                    rooms.size shouldBe 1
                    rooms.first().id shouldBe existingRoomId
                }
            }

            context("빈 리스트로 조회") {
                it("빈 리스트를 반환한다") {
                    val rooms = chatRoomRepository.findByIds(emptyList())

                    rooms.size shouldBe 0
                }
            }

            context("모두 존재하지 않는 ID로 조회") {
                it("빈 리스트를 반환한다") {
                    val rooms = chatRoomRepository.findByIds(listOf(99998L, 99999L))

                    rooms.size shouldBe 0
                }
            }

            context("중복된 ID로 조회") {
                it("중복 제거되어 한 번만 반환된다") {
                    val roomId = chatRoomRepository.create(
                        name = "중복테스트방",
                        roomType = RoomType.GROUP,
                        createdAt = OffsetDateTime.now(ZoneOffset.UTC)
                    )

                    val rooms = chatRoomRepository.findByIds(listOf(roomId, roomId, roomId))

                    rooms.size shouldBe 1
                    rooms.first().id shouldBe roomId
                }
            }
        }

        describe("채팅방 생성 시간 검증") {

            context("채팅방 생성 시 시간 정보") {
                it("createdAt이 올바르게 저장된다") {
                    val now = OffsetDateTime.now(ZoneOffset.UTC)
                    val roomId = chatRoomRepository.create(
                        name = "시간테스트방",
                        roomType = RoomType.GROUP,
                        createdAt = now
                    )

                    val room = chatRoomRepository.findById(roomId)

                    room shouldNotBe null
                    // 초 단위까지만 비교 (밀리초 차이 무시)
                    room?.createdAt?.toEpochSecond() shouldBe now.toEpochSecond()
                }
            }
        }

        describe("채팅방 타입별 생성 검증") {

            context("다양한 타입의 채팅방 생성") {
                it("DIRECT와 GROUP 모두 정상 생성된다") {
                    val directRoomId = chatRoomRepository.create(
                        name = null,
                        roomType = RoomType.DIRECT,
                        createdAt = OffsetDateTime.now(ZoneOffset.UTC)
                    )

                    val groupRoomId = chatRoomRepository.create(
                        name = "그룹채팅",
                        roomType = RoomType.GROUP,
                        createdAt = OffsetDateTime.now(ZoneOffset.UTC)
                    )

                    val directRoom = chatRoomRepository.findById(directRoomId)
                    val groupRoom = chatRoomRepository.findById(groupRoomId)

                    directRoom?.roomType shouldBe RoomType.DIRECT
                    groupRoom?.roomType shouldBe RoomType.GROUP
                }
            }
        }
    }
}
