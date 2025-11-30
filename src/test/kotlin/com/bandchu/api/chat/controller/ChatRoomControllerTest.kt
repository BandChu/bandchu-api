package com.bandchu.api.chat.controller

import com.bandchu.api.domain.chat.dto.CreateChatRoomRequest
import com.bandchu.api.domain.chat.dto.UpdateReadStatusRequest
import com.bandchu.api.domain.chat.table.RoomType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ChatRoomControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    
    // ========== 기본 성공 케이스 ==========
    
    @Test
    fun `GROUP 채팅방 생성 성공`() {
        val request = CreateChatRoomRequest(
            roomType = RoomType.GROUP,
            name = "테스트 그룹",
            memberIds = listOf(2L, 3L)
        )
        
        mockMvc.perform(
            post("/api/chatrooms")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.roomId").exists())
            .andExpect(jsonPath("$.data.roomType").value("GROUP"))
            .andExpect(jsonPath("$.data.name").value("테스트 그룹"))
    }
    
    @Test
    fun `채팅방 목록 조회 성공`() {
        mockMvc.perform(
            get("/api/chatrooms")
                .header("X-User-Id", "1")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.rooms").isArray)
    }
    
    // ========== 에러 케이스 ==========
    
    @Test
    fun `DIRECT 채팅방에 memberIds 2명 이상 시 400 에러`() {
        val request = CreateChatRoomRequest(
            roomType = RoomType.DIRECT,
            name = null,
            memberIds = listOf(2L, 3L)
        )
        
        mockMvc.perform(
            post("/api/chatrooms")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("CHATROOM_INVALID_REQUEST"))
    }
    
    @Test
    fun `존재하지 않는 채팅방 읽음 처리 시 404 에러`() {
        val request = UpdateReadStatusRequest(lastReadMessageId = 1L)
        
        mockMvc.perform(
            patch("/api/chatrooms/99999/read")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("CHATROOM_NOT_FOUND"))
    }
}
