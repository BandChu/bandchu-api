package com.bandchu.api.domain.friend.service

import com.bandchu.api.domain.friend.dto.FriendResponse
import com.bandchu.api.domain.friend.repository.FriendRepository
import com.bandchu.api.domain.friend.table.FriendStatus
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.springframework.stereotype.Service

@Service
public class FriendService(
    private val friendRepository: FriendRepository
) {

    // 요청 목록 조회
    fun getFriendRequests(memberId: Long): List<FriendResponse> {
        return friendRepository.findAllReqByMemberId(memberId)
    }

    // 친구 요청 보내기
    fun sendFriendRequest(senderId: Long, receiverId: Long): FriendResponse {
        if (senderId == receiverId) {
            throw BusinessException(ErrorCode.FRIEND_SELF_REQUEST)
        }

        return friendRepository.sendFriendRequest(senderId, receiverId)
            ?: throw BusinessException(ErrorCode.FRIEND_REQUEST_DUPLICATED)
    }

    // 친구 요청 수락
    fun acceptFriendRequest(requestId: Long, currentMemberId: Long) {
        val success = friendRepository.acceptFriendRequest(requestId, currentMemberId)
        if (!success) {
            throw BusinessException(ErrorCode.FRIEND_REQUEST_ACCEPT_FAIL)
        }
    }

    // 친구 요청 거절
    fun rejectFriendRequest(requestId: Long, currentMemberId: Long) {
        val success = friendRepository.rejectFriendRequest(requestId, currentMemberId)
        if (!success) {
            throw BusinessException(ErrorCode.FRIEND_REQUEST_REJECT_FAIL)
        }
    }

    // 친구 목록 조회
    fun getFriends(memberId: Long): List<FriendResponse> {
        return friendRepository.findAllFriends(memberId)
            .filter { it.status == FriendStatus.ACCEPTED } // 수락된 친구만 반환
    }
}