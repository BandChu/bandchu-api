package com.bandchu.api.domain.friend.controller

import com.bandchu.api.domain.friend.dto.FriendResponse
import com.bandchu.api.domain.friend.service.FriendService
import com.bandchu.api.global.response.ApiResponse
import com.bandchu.api.global.util.getCurrentUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/friends")
@Tag(name = "Friend", description = "친구 맺기 관련 API")
class FriendController(
    private val friendService: FriendService
) {
    @Operation(summary = "친구요청 목록 조회", description = "친구요청 목록조회하는 것인데 보낸 요청은 true, 받은 요청은 false로 보여주고 있습니다.")
    // 친구 요청 목록 조회 (보낸 요청은 true, 받은 요청은 false)
    @GetMapping("/requests")
    fun getFriendRequests(): ApiResponse<List<FriendResponse>> {
        val memberId = getCurrentUserId()
        val result = friendService.getFriendRequests(memberId)
        return ApiResponse.success(
            data = result,
            message = "친구 요청 목록을 조회하였습니다."
        )
    }
    @Operation(summary = "친구 요청 보내기", description = "해당 ID로 친구 요청을 보냅니다.")
    // 친구 요청 보내기
    @PostMapping("/requests")
    fun sendFriendRequest(@RequestParam receiverId: Long): ApiResponse<FriendResponse> {
        val senderId = getCurrentUserId()
        val result = friendService.sendFriendRequest(senderId, receiverId)
        return ApiResponse.success(
            data = result,
            message = "친구 요청을 보냈습니다."
        )
    }
    @Operation(summary = "친구 요청 수락하기", description = "받은 요청에 대해 친구 요청을 수락해 친구관계를 맺습니다.")
    // 친구 요청 수락
    @PostMapping("/requests/{requestId}/accept")
    fun acceptFriendRequest(@PathVariable requestId: Long): ApiResponse<Nothing?> {
        val memberId = getCurrentUserId()
        friendService.acceptFriendRequest(requestId, memberId)
        return ApiResponse.success(
            data = null,
            message = "친구 요청을 수락했습니다."
        )
    }
    @Operation(summary = "친구 요청 거절", description = "친구 요청을 거절합니다. ")
    // 친구 요청 거절
    @PostMapping("/requests/{requestId}/reject")
    fun rejectFriendRequest(@PathVariable requestId: Long): ApiResponse<Nothing?> {
        val memberId = getCurrentUserId()
        friendService.rejectFriendRequest(requestId, memberId)
        return ApiResponse.success(
            data = null,
            message = "친구 요청을 거절했습니다."
        )
    }
    @Operation(summary = "친구 목록 조회", description = "친구 목록에 있는 친구들을 조회합니다.")
    // 친구 목록 조회 (수락된 친구만)
    @GetMapping
    fun getFriends(): ApiResponse<List<FriendResponse>> {
        val memberId = getCurrentUserId()
        val result = friendService.getFriends(memberId)
        return ApiResponse.success(
            data = result,
            message = "친구 목록을 조회했습니다."
        )
    }
}