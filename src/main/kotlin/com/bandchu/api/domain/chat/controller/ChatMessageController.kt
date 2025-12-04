package com.bandchu.api.domain.chat.controller

import com.bandchu.api.domain.chat.dto.ChatMessageResponse
import com.bandchu.api.domain.chat.dto.MessagePageResponse
import com.bandchu.api.domain.chat.dto.SendMessageRequest
import com.bandchu.api.domain.chat.service.ChatMessageService
import com.bandchu.api.global.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/api/chatrooms"])
class ChatMessageController(private val chatMessageService: ChatMessageService) {

    @PostMapping("/{roomId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    fun sendMessage(
            @PathVariable roomId: Long,
            @RequestBody chatMessageRequest: SendMessageRequest
    ): ApiResponse<ChatMessageResponse> {
        // SecurityContext에서 memberId 가져오기
        val authentication = SecurityContextHolder.getContext().authentication
        val senderId = authentication.principal as Long

        val message =
                chatMessageService.sendMessage(
                        roomId = roomId,
                        senderId = senderId,
                        req = chatMessageRequest
                )

        return ApiResponse(success = true, data = message, message = "요청이 성공적으로 처리되었습니다.")
    }

    @GetMapping("/{roomId}/messages")
    fun getMessages(
            @PathVariable roomId: Long,
            @RequestParam(required = false) cursor: Long?,
            @RequestParam(defaultValue = "30") size: Int
    ): ApiResponse<MessagePageResponse> {
        val result = chatMessageService.fetchMessages(roomId, cursor, size)

        return ApiResponse.success(data = result, message = "요청이 성공적으로 처리되었습니다.")
    }
}
