package com.bandchu.api.domain.friend.dto

import com.bandchu.api.domain.friend.table.FriendStatus
import com.bandchu.api.domain.friend.table.FriendTable
import io.swagger.v3.oas.annotations.media.Schema
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.OffsetDateTime

data class FriendResponse(
    @get:Schema(description = "친구 테이블의 고유 ID", example = "101")
    val id: Long,

    @get:Schema(description = "친구 요청 보낸 사람의 member id", example = "11")
    val senderId: Long,

    @get:Schema(description = "친구 요청 받은 사람의 member id", example = "22")
    val receiverId: Long,

    @get:Schema(description = "친구 요청 상태 PENDING, ACCEPTED 이렇게 두가지 상태가 있습니다.", example = "PENDING, ACCEPTED")
    val status: FriendStatus,

    @get:Schema(description = "친구 객체 생성 날짜", example = "2026:01:01 ")
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