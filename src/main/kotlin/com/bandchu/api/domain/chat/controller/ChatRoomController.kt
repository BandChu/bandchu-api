package com.bandchu.api.domain.chat.controller

import com.bandchu.api.chat.dto.*
import com.bandchu.api.chat.service.ChatRoomService
import com.bandchu.api.global.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * 채팅방 Controller
 * - 채팅방 생성, 목록 조회, 읽음 처리 API 엔드포인트
 */
@RestController
@RequestMapping("/api/chatrooms")
class ChatRoomController(
    private val chatRoomService: ChatRoomService
) {

    /**
     * 1. 채팅방 생성
     * POST /api/chatrooms
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createChatRoom(
        @RequestBody request: CreateChatRoomRequest,
        // TODO: JWT에서 추출, 임시로 1L 사용
        @RequestHeader(value = "X-User-Id", required = false) currentUserId: Long? = 1L
    ): ApiResponse<CreateChatRoomResponse> {
        val response = chatRoomService.createChatRoom(request, currentUserId!!)
        return ApiResponse.success(response)
    }

    /**
     * 3. 채팅방 목록 조회
     * GET /api/chatrooms
     */
    @GetMapping
    fun getChatRoomList(
        // TODO: JWT에서 추출, 임시로 1L 사용
        @RequestHeader(value = "X-User-Id", required = false) currentUserId: Long? = 1L
    ): ApiResponse<ChatRoomListResponse> {
        val response = chatRoomService.getChatRoomList(currentUserId!!)
        return ApiResponse.success(response)
    }

    /**
     * 5. 메시지 읽음 처리
     * PATCH /api/chatrooms/{roomId}/read
     */
    @PatchMapping("/{roomId}/read")
    fun updateReadStatus(
        @PathVariable roomId: Long,
        @RequestBody request: UpdateReadStatusRequest,
        // TODO: JWT에서 추출, 임시로 1L 사용
        @RequestHeader(value = "X-User-Id", required = false) currentUserId: Long? = 1L
    ): ApiResponse<UpdateReadStatusResponse> {
        val response = chatRoomService.updateReadStatus(roomId, request, currentUserId!!)
        return ApiResponse.success(response)
    }
}
