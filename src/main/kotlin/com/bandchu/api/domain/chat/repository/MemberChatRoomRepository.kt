package com.bandchu.api.domain.chat.repository

import com.bandchu.api.domain.chat.table.MemberChatRoomTable
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

/**
 * 회원-채팅방 연결 Repository
 * - 채팅방 참여자 관리 및 읽음 상태 업데이트
 */
@Repository
class MemberChatRoomRepository {

    /**
     * 채팅방에 회원 추가
     */
    fun addMember(roomId: Long, memberId: Long, role: String = "MEMBER", joinedAt: OffsetDateTime): Long {
        return MemberChatRoomTable.insert {
            it[MemberChatRoomTable.roomId] = roomId
            it[MemberChatRoomTable.memberId] = memberId
            it[MemberChatRoomTable.role] = role
            it[MemberChatRoomTable.lastReadMessageId] = null
            it[MemberChatRoomTable.joinedAt] = joinedAt
        } get MemberChatRoomTable.id
    }

    /**
     * 특정 회원이 속한 모든 채팅방 ID 조회
     */
    fun findRoomIdsByMemberId(memberId: Long): List<Long> {
        return MemberChatRoomTable.selectAll()
            .where { MemberChatRoomTable.memberId eq memberId }
            .map { it[MemberChatRoomTable.roomId] }
    }

    /**
     * 특정 채팅방의 모든 참여자 ID 조회
     */
    fun findMemberIdsByRoomId(roomId: Long): List<Long> {
        return MemberChatRoomTable.selectAll()
            .where { MemberChatRoomTable.roomId eq roomId }
            .map { it[MemberChatRoomTable.memberId].value }
    }

    /**
     * 읽음 처리 (lastReadMessageId 업데이트)
     */
    fun updateLastReadMessageId(roomId: Long, memberId: Long, messageId: Long): Int {
        return MemberChatRoomTable.update(
            where = {
                (MemberChatRoomTable.roomId eq roomId) and
                (MemberChatRoomTable.memberId eq memberId)
            }
        ) {
            it[MemberChatRoomTable.lastReadMessageId] = messageId
        }
    }

    /**
     * 특정 회원의 특정 채팅방 lastReadMessageId 조회
     */
    fun findLastReadMessageId(roomId: Long, memberId: Long): Long? {
        return MemberChatRoomTable.selectAll()
            .where {
                (MemberChatRoomTable.roomId eq roomId) and
                (MemberChatRoomTable.memberId eq memberId)
            }
            .singleOrNull()
            ?.get(MemberChatRoomTable.lastReadMessageId)
    }

    /**
     * 두 사용자 간 공통 채팅방 찾기 (1:1 채팅방 중복 체크용)
     */
    fun findCommonDirectRoom(member1Id: Long, member2Id: Long): Long? {
        val member1Rooms = findRoomIdsByMemberId(member1Id).toSet()
        val member2Rooms = findRoomIdsByMemberId(member2Id).toSet()
        
        return member1Rooms.intersect(member2Rooms).firstOrNull { roomId ->
            findMemberIdsByRoomId(roomId).size == 2
        }
    }
}
