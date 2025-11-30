package com.bandchu.api.domain.chat.controller

import com.bandchu.api.domain.chat.dto.ChatMessageResponse
import com.bandchu.api.domain.chat.dto.MessagePageResponse
import com.bandchu.api.domain.chat.dto.SendMessageRequest
import com.bandchu.api.domain.chat.service.ChatMessageService
import com.bandchu.api.global.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/chatrooms"])
@CrossOrigin(origins = ["*"])
class ChatMessageController(
    private val chatMessageService: ChatMessageService,
    //private val jwtService : JwtService
) {
    @PostMapping("/{roomId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    fun sendMessage(@PathVariable roomId: Long,
                    @RequestBody chatMessageRequest: SendMessageRequest,
                    @RequestHeader("Authorization", required = false) token: String?) : ApiResponse<ChatMessageResponse> {
        val senderId = 1L //JWT Utility 클래스에서 가져오기
        val message = chatMessageService.sendMessage(roomId, senderId, chatMessageRequest)

        return ApiResponse(
            success = true,
            data = message,
            message = "요청이 성공적으로 처리되었습니다."
        )
    }

    @GetMapping("/{roomId}/messages")
    fun getMessages(
        @PathVariable roomId: Long,
        @RequestParam(required = false) cursor: Long?,
        @RequestParam size: Int = 10
    ): ApiResponse<MessagePageResponse> {

        val result = chatMessageService.fetchMessages(roomId, cursor, size)

        return ApiResponse.success(
            data = result,
            message = "요청이 성공적으로 처리되었습니다."
        )
    }

}