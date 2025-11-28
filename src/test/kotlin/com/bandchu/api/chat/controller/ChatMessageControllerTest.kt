package com.bandchu.api.chat.controller

import com.bandchu.api.chat.dto.ChatMessageResponse
import com.bandchu.api.chat.dto.SendMessageRequest
import com.bandchu.api.chat.service.ChatMessageService
import com.bandchu.api.domain.chat.dto.SendMessageRequest
import com.bandchu.api.domain.chat.table.MessageType
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

class ChatMessageControllerTest : FunSpec({
    // ChatMessageService의 mock 객체 생성
    val chatMessageService = mockk<ChatMessageService>()
    val controller = ChatMessageController(chatMessageService)

    // MockMvc 설정: Spring MVC 테스트를 위한 객체
    val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(controller).build()

    // JSON 직렬화/역직렬화를 위한 ObjectMapper
    val objectMapper = ObjectMapper()

    // === 테스트 1: 정상적인 메시지 전송 ===
    test("메시지 전송 성공 시 201 CREATED와 메시지 정보 반환") {
        // given
        val roomId = 1L

        // 요청 본문으로 보낼 메시지 데이터
        val request = SendMessageRequest(
            messageType = MessageType.TEXT,
            content = "안녕하세요",
            fileUrl = null
        )

        // 서비스에서 반환될 예상 응답
        val expectedResponse = ChatMessageResponse(
            messageId = 1L,
            roomId = roomId,
            senderId = 1L,  // JWT에서 추출된 사용자 ID (현재는 하드코딩)
            messageType = MessageType.TEXT,
            content = "안녕하세요",
            fileUrl = null,
            createdAt = OffsetDateTime.now(java.time.ZoneOffset.UTC)
        )

        // 서비스 메서드 호출 시 동작 정의
        every { chatMessageService.sendMessage(roomId, 1L, request) } returns expectedResponse

        // when & then: HTTP 요청 실행 및 검증
        mockMvc.perform(
            post("/api/chatrooms/{roomId}/messages", roomId)  // POST 요청 URL
                .contentType(MediaType.APPLICATION_JSON)  // Content-Type: application/json
                .header("Authorization", "Bearer test-token")  // Authorization 헤더 추가
                .content(objectMapper.writeValueAsString(request))  // 요청 본문을 JSON으로 변환
        )
            // HTTP 상태 코드가 201 CREATED인지 검증
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
            .andExpect(jsonPath("$.data.messageId").value(1L))
            .andExpect(jsonPath("$.data.roomId").value(roomId))
            .andExpect(jsonPath("$.data.senderId").value(1L))
            .andExpect(jsonPath("$.data.messageType").value("TEXT"))
            .andExpect(jsonPath("$.data.content").value("안녕하세요"))

        // 서비스 메서드가 정확히 1번 호출되었는지 검증
        verify(exactly = 1) {
            chatMessageService.sendMessage(roomId, 1L, request)
        }
    }

    // === 테스트 2: 파일 메시지 전송 ===
    test("파일 메시지 전송 시 fileUrl이 포함되어야 함") {
        // given
        val roomId = 1L
        val request = SendMessageRequest(
            messageType = MessageType.IMAGE,
            content = null,  // 이미지 메시지는 content가 null
            fileUrl = "https://example.com/image.jpg"
        )

        val expectedResponse = ChatMessageResponse(
            messageId = 2L,
            roomId = roomId,
            senderId = 1L,
            messageType = MessageType.IMAGE,
            content = null,
            fileUrl = "https://example.com/image.jpg",
            createdAt = OffsetDateTime.now(java.time.ZoneOffset.UTC)
        )

        every { chatMessageService.sendMessage(roomId, 1L, request) } returns expectedResponse

        // when & then
        mockMvc.perform(
            post("/api/chatrooms/{roomId}/messages", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-token")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.messageType").value("IMAGE"))
            .andExpect(jsonPath("$.data.fileUrl").value("https://example.com/image.jpg"))
            .andExpect(jsonPath("$.data.content").isEmpty)  // content가 null인지 검증
    }

    // === 테스트 3: 권한 없는 사용자의 메시지 전송 시도 ===
    test("채팅방 참여자가 아닐 경우 403 FORBIDDEN 반환") {
        // given
        val roomId = 1L
        val request = SendMessageRequest(
            messageType = MessageType.TEXT,
            content = "권한 없는 메시지",
            fileUrl = null
        )

        // 서비스에서 FORBIDDEN 예외를 던지도록 설정
        every { chatMessageService.sendMessage(roomId, 1L, request) } throws
                ResponseStatusException(HttpStatus.FORBIDDEN, "채팅방 참여자가 아닙니다")

        // when & then
        mockMvc.perform(
            post("/api/chatrooms/{roomId}/messages", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-token")
                .content(objectMapper.writeValueAsString(request))
        )
            // HTTP 상태 코드가 403 FORBIDDEN인지 검증
            .andExpect(status().isForbidden)
    }

    // === 테스트 4: Authorization 헤더 누락 ===
    test("Authorization 헤더가 없을 경우 400 BAD REQUEST 반환") {
        // given
        val roomId = 1L
        val request = SendMessageRequest(
            messageType = MessageType.TEXT,
            content = "테스트 메시지",
            fileUrl = null
        )

        // when & then
        mockMvc.perform(
            post("/api/chatrooms/{roomId}/messages", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                // Authorization 헤더를 의도적으로 누락
                .content(objectMapper.writeValueAsString(request))
        )
            // 400 또는 401 응답이 예상됨 (실제 동작은 Spring Security 설정에 따라 다름)
            .andExpect(status().is4xxClientError)
    }

    // === 테스트 5: 잘못된 요청 본문 (messageType 누락) ===
    test("필수 필드 누락 시 400 BAD REQUEST 반환") {
        // given
        val roomId = 1L
        // messageType이 누락된 잘못된 JSON
        val invalidJson = """
            {
                "content": "테스트 메시지"
            }
        """.trimIndent()

        // when & then
        mockMvc.perform(
            post("/api/chatrooms/{roomId}/messages", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-token")
                .content(invalidJson)  // 유효하지 않은 JSON
        )
            // 400 BAD REQUEST 응답 예상
            .andExpect(status().isBadRequest)
    }

    // === 테스트 6: 빈 content로 텍스트 메시지 전송 ===
    test("빈 content로 TEXT 메시지 전송 가능") {
        // given
        val roomId = 1L
        val request = SendMessageRequest(
            messageType = MessageType.TEXT,
            content = "",  // 빈 문자열
            fileUrl = null
        )

        val expectedResponse = ChatMessageResponse(
            messageId = 3L,
            roomId = roomId,
            senderId = 1L,
            messageType = MessageType.TEXT,
            content = "",
            fileUrl = null,
            createdAt = OffsetDateTime.now(java.time.ZoneOffset.UTC)
        )

        every { chatMessageService.sendMessage(roomId, 1L, request) } returns expectedResponse

        // when & then
        mockMvc.perform(
            post("/api/chatrooms/{roomId}/messages", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-token")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.content").value(""))
    }
})