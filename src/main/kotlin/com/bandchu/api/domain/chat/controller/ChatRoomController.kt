package com.bandchu.api.domain.chat.controller

import com.bandchu.api.domain.chat.dto.ChatRoomDetailResponse
import com.bandchu.api.domain.chat.dto.ChatRoomListResponse
import com.bandchu.api.domain.chat.dto.CreateChatRoomRequest
import com.bandchu.api.domain.chat.dto.CreateChatRoomResponse
import com.bandchu.api.domain.chat.dto.UpdateReadStatusRequest
import com.bandchu.api.domain.chat.dto.UpdateReadStatusResponse
import com.bandchu.api.domain.chat.service.ChatRoomService
import com.bandchu.api.global.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/api/chatrooms"])
class ChatRoomController(private val chatRoomService: ChatRoomService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createChatRoom(
            @RequestBody request: CreateChatRoomRequest
    ): ApiResponse<CreateChatRoomResponse> {
        // SecurityContext에서 memberId 가져오기
        val authentication = SecurityContextHolder.getContext().authentication
        val currentUserId = authentication.principal as Long

        val chatRoom = chatRoomService.createChatRoom(request, currentUserId)

        return ApiResponse(success = true, data = chatRoom, message = "요청이 성공적으로 처리되었습니다.")
    }

    @GetMapping
    fun getChatRoomList(): ApiResponse<ChatRoomListResponse> {
        // SecurityContext에서 memberId 가져오기
        val authentication = SecurityContextHolder.getContext().authentication
        val currentUserId = authentication.principal as Long

        val chatRooms = chatRoomService.getChatRoomList(currentUserId)

        return ApiResponse.success(data = chatRooms, message = "요청이 성공적으로 처리되었습니다.")
    }

    @GetMapping("/{roomId}")
    fun getChatRoomDetail(@PathVariable roomId: Long): ApiResponse<ChatRoomDetailResponse> {
        // SecurityContext에서 memberId 가져오기
        val authentication = SecurityContextHolder.getContext().authentication
        val currentUserId = authentication.principal as Long

        val chatRoomDetail = chatRoomService.getChatRoomDetail(roomId, currentUserId)

        return ApiResponse.success(data = chatRoomDetail, message = "요청이 성공적으로 처리되었습니다.")
    }

    @PutMapping("/{roomId}/read-status")
    fun updateReadStatus(
            @PathVariable roomId: Long,
            @RequestBody request: UpdateReadStatusRequest
    ): ApiResponse<UpdateReadStatusResponse> {
        // SecurityContext에서 memberId 가져오기
        val authentication = SecurityContextHolder.getContext().authentication
        val currentUserId = authentication.principal as Long

        val response = chatRoomService.updateReadStatus(roomId, request, currentUserId)

        return ApiResponse.success(data = response, message = "요청이 성공적으로 처리되었습니다.")
    }
}
