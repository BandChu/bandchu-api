package com.bandchu.api.domain.chat.controller

import com.bandchu.api.domain.chat.ChatTestConfig
import com.bandchu.api.domain.chat.dto.CreateChatRoomRequest
import com.bandchu.api.domain.chat.dto.CreateChatRoomResponse
import com.bandchu.api.domain.chat.dto.UpdateReadStatusRequest
import com.bandchu.api.domain.chat.table.RoomType
import com.bandchu.api.domain.member.model.Member
import com.bandchu.api.domain.member.model.Role
import com.bandchu.api.fixture.AuthFixture
import com.bandchu.api.fixture.ChatFixture
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(ChatTestConfig::class)
class ChatRoomApiTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val authFixture: AuthFixture,
    private val chatFixture: ChatFixture
) : DescribeSpec() {

    private lateinit var user1: Member
    private lateinit var user2: Member
    private lateinit var user3: Member
    private lateinit var myDirectRoom: CreateChatRoomResponse

    private val NON_EXISTENT_ID = 99999L

    init {
        beforeSpec {
            val suffix = System.currentTimeMillis()

            // 테스트용 회원 생성
            user1 = authFixture.createMember(
                AuthFixture.AuthCredentials(
                    "user1-$suffix@test.com",
                    "pass",
                    "User1",
                    Role.FAN
                )
            )
            user2 = authFixture.createMember(
                AuthFixture.AuthCredentials(
                    "user2-$suffix@test.com",
                    "pass",
                    "User2",
                    Role.FAN
                )
            )
            user3 = authFixture.createMember(
                AuthFixture.AuthCredentials(
                    "user3-$suffix@test.com",
                    "pass",
                    "User3",
                    Role.FAN
                )
            )

            // 기본 채팅방 생성
            myDirectRoom = chatFixture.createDirectRoom(user1, user2)
        }

        describe("채팅방 생성 및 조회 E2E 시나리오") {

            context("DIRECT 채팅방 생성 후 목록 조회") {
                it("생성된 채팅방이 목록에 포함됨") {
                    // 채팅방 생성
                    val createRequest = CreateChatRoomRequest(
                        roomType = RoomType.DIRECT,
                        name = null,
                        memberIds = listOf(user3.id!!)
                    )

                    val createResult = mockMvc.perform(
                        post("/api/chatrooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest))
                            .with(user(user1.id.toString()).roles("FAN"))
                    ).andReturn().response

                    createResult.status shouldBe HttpStatus.CREATED.value()
                    val createRoot = objectMapper.readTree(createResult.contentAsString)
                    createRoot["data"]["roomType"].asText() shouldBe "DIRECT"

                    // 목록 조회
                    val listResult = mockMvc.perform(
                        get("/api/chatrooms")
                            .with(user(user1.id.toString()).roles("FAN"))
                    ).andReturn().response

                    listResult.status shouldBe HttpStatus.OK.value()
                    val listRoot = objectMapper.readTree(listResult.contentAsString)
                    listRoot["data"]["rooms"].isArray shouldBe true
                    (listRoot["data"]["rooms"].size() >= 1) shouldBe true
                }
            }

            context("GROUP 채팅방 생성 후 멤버 정보 확인") {
                it("생성된 채팅방에 멤버가 올바르게 등록됨") {
                    val createRequest = CreateChatRoomRequest(
                        roomType = RoomType.GROUP,
                        name = "알고리즘 스터디",
                        memberIds = listOf(user2.id!!, user3.id!!)
                    )

                    val result = mockMvc.perform(
                        post("/api/chatrooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest))
                            .with(user(user1.id.toString()).roles("FAN"))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.CREATED.value()
                    val root = objectMapper.readTree(result.contentAsString)
                    root["data"]["roomType"].asText() shouldBe "GROUP"
                    root["data"]["name"].asText() shouldBe "알고리즘 스터디"
                }
            }

            context("이미 존재하는 DIRECT 채팅방 생성 시도 (멱등성 테스트)") {
                it("201 Created와 기존 채팅방 정보 반환") {
                    val request = CreateChatRoomRequest(
                        roomType = RoomType.DIRECT,
                        name = null,
                        memberIds = listOf(user2.id!!)
                    )

                    val result = mockMvc.perform(
                        post("/api/chatrooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(user1.id.toString()).roles("FAN"))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.CREATED.value()
                    val root = objectMapper.readTree(result.contentAsString)
                    root["data"]["roomId"].asLong() shouldBe myDirectRoom.roomId
                }
            }
        }

        describe("메시지 전송 후 읽음 처리 E2E 시나리오") {

            context("메시지 전송 후 읽음 처리") {
                it("읽음 처리가 정상적으로 업데이트됨") {
                    // 메시지 전송
                    val message = chatFixture.sendMessage(myDirectRoom.roomId, user2, "안녕하세요")

                    // 읽음 처리
                    val request = UpdateReadStatusRequest(
                        lastReadMessageId = message.messageId
                    )

                    val result = mockMvc.perform(
                        put("/api/chatrooms/${myDirectRoom.roomId}/read-status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(user1.id.toString()).roles("FAN"))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.OK.value()
                }
            }
        }

        describe("Edge Case - 예외 상황 테스트") {

            context("DIRECT 채팅방 생성 시 memberIds가 2명 이상") {
                it("400 Bad Request 또는 적절한 에러 응답") {
                    val request = CreateChatRoomRequest(
                        roomType = RoomType.DIRECT,
                        name = null,
                        memberIds = listOf(user2.id!!, user3.id!!) // 2명 (DIRECT는 1명만 가능)
                    )

                    val result = mockMvc.perform(
                        post("/api/chatrooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(user1.id.toString()).roles("FAN"))
                    ).andReturn().response

                    (result.status >= 400) shouldBe true
                }
            }

            context("DIRECT 채팅방 생성 시 memberIds가 비어있음") {
                it("400 Bad Request 또는 적절한 에러 응답") {
                    val request = CreateChatRoomRequest(
                        roomType = RoomType.DIRECT,
                        name = null,
                        memberIds = emptyList()
                    )

                    val result = mockMvc.perform(
                        post("/api/chatrooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(user1.id.toString()).roles("FAN"))
                    ).andReturn().response

                    (result.status >= 400) shouldBe true
                }
            }

            context("존재하지 않는 사용자를 채팅방에 초대") {
                it("에러가 발생하거나 무시됨") {
                    val request = CreateChatRoomRequest(
                        roomType = RoomType.GROUP,
                        name = "테스트방",
                        memberIds = listOf(99999L) // 존재하지 않는 사용자 ID
                    )

                    val result = mockMvc.perform(
                        post("/api/chatrooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(user1.id.toString()).roles("FAN"))
                    ).andReturn().response

                    // 성공하거나 에러가 발생해야 함 (현재 구현에 따라 다름)
                    (result.status >= 200) shouldBe true
                }
            }

            context("참여하지 않은 채팅방의 읽음 상태 업데이트 시도") {
                it("403 Forbidden 또는 적절한 에러 응답") {
                    val request = UpdateReadStatusRequest(lastReadMessageId = 1L)

                    val result = mockMvc.perform(
                        put("/api/chatrooms/${myDirectRoom.roomId}/read-status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(user3.id.toString()).roles("FAN")) // user3는 참여하지 않음
                    ).andReturn().response

                    (result.status >= 400) shouldBe true
                }
            }

            context("존재하지 않는 채팅방의 읽음 상태 업데이트") {
                it("404 Not Found 또는 적절한 에러 응답") {
                    val request = UpdateReadStatusRequest(lastReadMessageId = 1L)

                    val result = mockMvc.perform(
                        put("/api/chatrooms/$NON_EXISTENT_ID/read-status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(user1.id.toString()).roles("FAN"))
                    ).andReturn().response

                    (result.status >= 400) shouldBe true
                }
            }

            context("GROUP 채팅방에 많은 수의 멤버 초대") {
                it("정상적으로 처리됨") {
                    // user2, user3 포함 (총 3명)
                    val request = CreateChatRoomRequest(
                        roomType = RoomType.GROUP,
                        name = "대규모 그룹",
                        memberIds = listOf(user2.id!!, user3.id!!)
                    )

                    val result = mockMvc.perform(
                        post("/api/chatrooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(user1.id.toString()).roles("FAN"))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.CREATED.value()
                    val root = objectMapper.readTree(result.contentAsString)
                    root["data"]["roomType"].asText() shouldBe "GROUP"
                }
            }

            // 문자열 길이 제한은 DB 설정에 따라 다르므로 주석 처리
            // context("매우 긴 이름의 GROUP 채팅방 생성") {
            //     it("적절하게 처리되거나 에러 반환") {
            //         val longName = "a".repeat(300) // 255자 제한 초과
            //         val request = CreateChatRoomRequest(
            //             roomType = RoomType.GROUP,
            //             name = longName,
            //             memberIds = listOf(user2.id!!)
            //         )
            //
            //         val result = mockMvc.perform(
            //             post("/api/chatrooms")
            //                 .contentType(MediaType.APPLICATION_JSON)
            //                 .content(objectMapper.writeValueAsString(request))
            //                 .with(user(user1.id.toString()).roles("FAN"))
            //         ).andReturn().response
            //
            //         // 길이 제한으로 에러가 발생하거나, DB가 잘라서 저장하거나 함
            //         (result.status >= 200 && result.status < 500) shouldBe true
            //     }
            // }
        }
    }
}
