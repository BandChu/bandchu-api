package com.bandchu.api.domain.chat.controller

import com.bandchu.api.domain.chat.dto.ChatMessageResponse
import com.bandchu.api.domain.chat.dto.MessagePageResponse
import com.bandchu.api.domain.chat.dto.SendMessageRequest
import com.bandchu.api.domain.chat.service.ChatMessageService
import com.bandchu.api.global.response.ApiResponse
import com.bandchu.api.global.util.getCurrentUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/api/chatrooms"])
@Tag(name = "ChatMessage", description = "채팅 메세지 관련 API")
class ChatMessageController(private val chatMessageService: ChatMessageService) {

    @Operation(summary = "메세지 전송", description = "해당하는 roomId에 메세지를 전송합니다")
    @PostMapping("/{roomId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    fun sendMessage(
            @PathVariable roomId: Long,
            @RequestBody chatMessageRequest: SendMessageRequest
    ): ApiResponse<ChatMessageResponse> {
        val senderId = getCurrentUserId()

        val message =
                chatMessageService.sendMessage(
                        roomId = roomId,
                        senderId = senderId,
                        req = chatMessageRequest
                )

        return ApiResponse(success = true, data = message, message = "요청이 성공적으로 처리되었습니다.")
    }

    @Operation(summary = "메세지 가져오기", description = "해당하는 roomId의 최신 메세지들을 가져옵니다. size: 30")
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
