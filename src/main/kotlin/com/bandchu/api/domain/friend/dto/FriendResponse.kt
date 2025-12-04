package com.bandchu.api.domain.friend.dto

import com.bandchu.api.domain.friend.table.FriendStatus
import com.bandchu.api.domain.friend.table.FriendTable
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.OffsetDateTime

data class FriendResponse(
    val id: Long,
    val senderId: Long,
    val receiverId: Long,
    val status: FriendStatus,
    val createdAt: OffsetDateTime,

    val isSentByMe: Boolean // true면 내가 보낸 요청, false면 내가 받은 요청
) {
    companion object {
        // ResultRow -> FriendResponse 변환
        fun ResultRow.toFriendResponse(currentMemberId: Long): FriendResponse {
            return FriendResponse(
                id = this[FriendTable.id],
                senderId = this[FriendTable.senderId],
                receiverId = this[FriendTable.receiverId],
                status = this[FriendTable.status],
                createdAt = this[FriendTable.createdAt],
                isSentByMe = this[FriendTable.senderId] == currentMemberId
            )
        }
    }
}