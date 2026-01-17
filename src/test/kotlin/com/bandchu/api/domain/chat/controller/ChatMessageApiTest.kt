package com.bandchu.api.domain.chat.controller

import com.bandchu.api.domain.chat.ChatTestConfig
import com.bandchu.api.domain.chat.dto.CreateChatRoomResponse
import com.bandchu.api.domain.chat.dto.SendMessageRequest
import com.bandchu.api.domain.chat.table.MessageType
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
class ChatMessageApiTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val authFixture: AuthFixture,
    private val chatFixture: ChatFixture
) : DescribeSpec() {

    private lateinit var sender: Member
    private lateinit var receiver: Member
    private lateinit var outsider: Member
    private lateinit var chatRoom: CreateChatRoomResponse

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

            chatRoom = chatFixture.createDirectRoom(sender, receiver)
        }

        describe("메시지 전송 및 조회 E2E 시나리오") {

            context("TEXT/IMAGE 메시지 전송 후 조회") {
                it("전송한 메시지들이 목록에 포함됨") {
                    // TEXT 메시지 전송
                    val textRequest = SendMessageRequest(
                        messageType = MessageType.TEXT,
                        content = "안녕하세요",
                        fileUrl = null
                    )

                    val textResult = mockMvc.perform(
                        post("/api/chatrooms/${chatRoom.roomId}/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(textRequest))
                            .with(user(sender.id.toString()).roles("FAN"))
                    ).andReturn().response

                    textResult.status shouldBe HttpStatus.CREATED.value()
                    val textRoot = objectMapper.readTree(textResult.contentAsString)
                    textRoot["data"]["messageType"].asText() shouldBe "TEXT"
                    textRoot["data"]["content"].asText() shouldBe "안녕하세요"

                    // IMAGE 메시지 전송
                    val imageRequest = SendMessageRequest(
                        messageType = MessageType.IMAGE,
                        content = null,
                        fileUrl = "https://example.com/image.jpg"
                    )

                    val imageResult = mockMvc.perform(
                        post("/api/chatrooms/${chatRoom.roomId}/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(imageRequest))
                            .with(user(sender.id.toString()).roles("FAN"))
                    ).andReturn().response

                    imageResult.status shouldBe HttpStatus.CREATED.value()
                    val imageRoot = objectMapper.readTree(imageResult.contentAsString)
                    imageRoot["data"]["messageType"].asText() shouldBe "IMAGE"
                    imageRoot["data"]["fileUrl"].asText() shouldBe "https://example.com/image.jpg"

                    // 메시지 목록 조회
                    val listResult = mockMvc.perform(
                        get("/api/chatrooms/${chatRoom.roomId}/messages")
                            .param("size", "10")
                            .with(user(sender.id.toString()).roles("FAN"))
                    ).andReturn().response

                    listResult.status shouldBe HttpStatus.OK.value()
                    val listRoot = objectMapper.readTree(listResult.contentAsString)
                    (listRoot["data"]["messages"].size() >= 2) shouldBe true
                }
            }

            context("커서 기반 페이징으로 대량 메시지 조회") {
                it("nextCursor를 사용한 페이징이 정상 동작함") {
                    // 메시지 20개 생성
                    chatFixture.sendMultipleMessages(chatRoom.roomId, sender, 20)

                    // 첫 페이지 조회
                    val firstPage = mockMvc.perform(
                        get("/api/chatrooms/${chatRoom.roomId}/messages")
                            .param("size", "10")
                            .with(user(sender.id.toString()).roles("FAN"))
                    ).andReturn().response

                    firstPage.status shouldBe HttpStatus.OK.value()
                    val firstRoot = objectMapper.readTree(firstPage.contentAsString)
                    val nextCursor = firstRoot["data"]["nextCursor"]

                    if (!nextCursor.isNull) {
                        // 두 번째 페이지 조회
                        val secondPage = mockMvc.perform(
                            get("/api/chatrooms/${chatRoom.roomId}/messages")
                                .param("cursor", nextCursor.asText())
                                .param("size", "10")
                                .with(user(sender.id.toString()).roles("FAN"))
                        ).andReturn().response

                        secondPage.status shouldBe HttpStatus.OK.value()
                        val secondRoot = objectMapper.readTree(secondPage.contentAsString)
                        secondRoot["data"]["messages"].size() shouldBe 10
                    }
                }
            }

            context("빈 채팅방 메시지 조회") {
                it("200 OK와 빈 목록 반환") {
                    val emptyRoom = chatFixture.createDirectRoom(sender, outsider)

                    val result = mockMvc.perform(
                        get("/api/chatrooms/${emptyRoom.roomId}/messages")
                            .param("size", "10")
                            .with(user(sender.id.toString()).roles("FAN"))
                    ).andReturn().response

                    result.status shouldBe HttpStatus.OK.value()
                    val root = objectMapper.readTree(result.contentAsString)
                    root["data"]["messages"].size() shouldBe 0
                }
            }
        }
    }
}
