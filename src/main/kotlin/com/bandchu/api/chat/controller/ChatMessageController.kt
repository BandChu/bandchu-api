package com.bandchu.api.chat.controller

import com.bandchu.api.chat.dto.ChatMessageResponse
import com.bandchu.api.chat.dto.SendMessageRequest
import com.bandchu.api.chat.service.ChatMessageService
import com.bandchu.api.global.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/chatrooms"])
class ChatMessageController(
    private val chatMessageService: ChatMessageService
) {
    @PostMapping("/{roomId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    fun sendMessage(@PathVariable roomId: Long,
                    @RequestBody chatMessageRequest: SendMessageRequest,
                    @RequestHeader("Authorization") token: String) : ApiResponse<ChatMessageResponse> {
        val senderId = 1L //JWT Utility 클래스에서 가져오기
        val message = chatMessageService.sendMessage(roomId, senderId, chatMessageRequest)

        return ApiResponse(
            success = true,
            data = message,
            message = "요청이 성공적으로 처리되었습니다."
        )
    }

}