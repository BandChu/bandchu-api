package com.bandchu.api.domain.chat.controller

import com.bandchu.api.domain.chat.dto.ChatRoomListResponse
import com.bandchu.api.domain.chat.dto.CreateChatRoomRequest
import com.bandchu.api.domain.chat.dto.CreateChatRoomResponse
import com.bandchu.api.domain.chat.dto.UpdateReadStatusRequest
import com.bandchu.api.domain.chat.dto.UpdateReadStatusResponse
import com.bandchu.api.domain.chat.service.ChatRoomService
import com.bandchu.api.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/api/chatrooms"])
@Tag(name = "ChatRoom", description = "채팅 방 관련 API")
class ChatRoomController(private val chatRoomService: ChatRoomService) {
    @Operation(summary = "채팅방 생성", description = "채팅방을 생성합니다. roomId가 생성됩니다.")
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
    @Operation(summary = "내 채팅방 리스트들 가져오기", description = "내가 속해 있는 채팅방들의 아이디를 가져옵니다. 이거로 내가 하고 있는 채팅들을 보게 하기 위해서입니다.")
    @GetMapping
    fun getChatRoomList(): ApiResponse<ChatRoomListResponse> {
        // SecurityContext에서 memberId 가져오기
        val authentication = SecurityContextHolder.getContext().authentication
        val currentUserId = authentication.principal as Long

        val chatRooms = chatRoomService.getChatRoomList(currentUserId)

        return ApiResponse.success(data = chatRooms, message = "요청이 성공적으로 처리되었습니다.")
    }
    @Operation(summary = "읽음 표시", description = "읽었는지 상태에 대한 변경을 합니다. 읽음, 안읽음 이런 표시입니다.")
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
