package com.bandchu.api.domain.friend.repository

import com.bandchu.api.domain.friend.table.FriendStatus
import com.bandchu.api.domain.friend.table.FriendTable
import com.bandchu.api.domain.friend.dto.FriendResponse
import com.bandchu.api.domain.friend.dto.FriendResponse.Companion.toFriendResponse
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.orWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class FriendRepository {
    //1. 친구 요청 목록 조회
    fun findAllReqByMemberId(memberId: Long) :List<FriendResponse> = transaction {
        FriendTable
            .selectAll()
            .where{ FriendTable.senderId eq  memberId }
            .orWhere { FriendTable.receiverId eq  memberId }
            .map {it.toFriendResponse(memberId)}
    }

    // 2. 친구 요청 보내기
    fun sendFriendRequest(senderId: Long, receiverId: Long): FriendResponse? = transaction {
        // 중복 요청 방지
        val exists = FriendTable
            .selectAll()
            .where{ (FriendTable.senderId eq senderId and (FriendTable.receiverId eq receiverId)) or
                    (FriendTable.senderId eq receiverId and (FriendTable.receiverId eq senderId))
        }.firstOrNull()

        if (exists != null) return@transaction null

        val inserted = FriendTable.insert {
            it[FriendTable.senderId] = senderId
            it[FriendTable.receiverId] = receiverId
            it[FriendTable.status] = FriendStatus.PENDING
            it[FriendTable.createdAt] = OffsetDateTime.now()
        }

        val insertId = inserted[FriendTable.id]

        FriendTable.selectAll()
            .where{ FriendTable.id eq insertId }
            .map { it.toFriendResponse(senderId) }
            .firstOrNull()
    }

    // 3. 친구 요청 수락
    fun acceptFriendRequest(requestId: Long, currentMemberId: Long): Boolean = transaction {
        val updated = FriendTable
            .update({ FriendTable.id eq requestId and (FriendTable.receiverId eq currentMemberId) }) {
            it[status] = FriendStatus.ACCEPTED
        }
        updated > 0
    }

    // 4. 친구 요청 거절
    fun rejectFriendRequest(requestId: Long, currentMemberId: Long): Boolean = transaction {
        val deleted = FriendTable
            .deleteWhere { (FriendTable.id eq requestId) and (FriendTable.receiverId eq currentMemberId) }
        deleted > 0
    }

    // 5. 친구 목록 조회
    fun findAllFriends(memberId: Long): List<FriendResponse> = transaction {
        FriendTable
            .selectAll()
            .where{ (FriendTable.senderId eq memberId) or (FriendTable.receiverId eq memberId) }
            .map { it.toFriendResponse(memberId) }
    }
}