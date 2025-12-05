package com.bandchu.api.domain.chat.service

import com.bandchu.api.domain.chat.dto.ChatRoomDetailResponse
import com.bandchu.api.domain.chat.dto.ChatRoomListResponse
import com.bandchu.api.domain.chat.dto.ChatRoomMemberDetail
import com.bandchu.api.domain.chat.dto.ChatRoomSummary
import com.bandchu.api.domain.chat.dto.CreateChatRoomRequest
import com.bandchu.api.domain.chat.dto.CreateChatRoomResponse
import com.bandchu.api.domain.chat.dto.UpdateReadStatusRequest
import com.bandchu.api.domain.chat.dto.UpdateReadStatusResponse
import com.bandchu.api.domain.chat.repository.ChatRoomRepository
import com.bandchu.api.domain.chat.repository.MemberChatRoomRepository
import com.bandchu.api.domain.chat.repository.ChatMessageRepository
import com.bandchu.api.domain.chat.table.RoomType
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * 채팅방 Service
 * - 채팅방 생성, 목록 조회, 읽음 처리 비즈니스 로직
 */
@Service
class ChatRoomService(
    private val chatRoomRepository: ChatRoomRepository,
    private val memberChatRoomRepository: MemberChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository
) {

    /**
     * 1. 채팅방 생성
     * - DIRECT: memberIds는 1명만 (상대방)
     * - GROUP: memberIds는 2명 이상
     * - 본인은 자동으로 포함됨
     */
    fun createChatRoom(request: CreateChatRoomRequest, currentUserId: Long): CreateChatRoomResponse {
        return transaction {
            if (request.roomType == RoomType.DIRECT) {
                if (request.memberIds.size != 1) {
                    throw BusinessException(ErrorCode.CHATROOM_INVALID_REQUEST)
                }

                val partnerId = request.memberIds.first()
                val existingRoomId = memberChatRoomRepository.findCommonDirectRoom(currentUserId, partnerId)

                // 🔥 이미 있으면 그냥 그 방 정보 반환
                if (existingRoomId != null) {
                    val existingRoom = chatRoomRepository.findById(existingRoomId)
                        ?: throw BusinessException(ErrorCode.CHATROOM_NOT_FOUND)

                    return@transaction CreateChatRoomResponse(
                        roomId = existingRoomId,
                        roomType = RoomType.DIRECT,
                        name = existingRoom.name,
                        createdAt = existingRoom.createdAt
                    )
                }
            }

            val now = OffsetDateTime.now(ZoneOffset.UTC)

            // 새 채팅방 생성
            val roomName = when (request.roomType) {
                RoomType.DIRECT -> null
                RoomType.GROUP -> request.name ?: "Unnamed Group"
                else -> request.name ?: "Unnamed Room"
            }

            val roomId = chatRoomRepository.create(
                name = roomName,
                roomType = request.roomType,
                createdAt = now
            )

            // 본인 추가
            memberChatRoomRepository.addMember(
                roomId = roomId,
                memberId = currentUserId,
                role = "OWNER",
                joinedAt = now
            )

            // 초대된 회원들 추가
            request.memberIds.forEach { memberId ->
                memberChatRoomRepository.addMember(
                    roomId = roomId,
                    memberId = memberId,
                    role = "MEMBER",
                    joinedAt = now
                )
            }

            CreateChatRoomResponse(
                roomId = roomId,
                roomType = request.roomType,
                name = roomName,
                createdAt = now
            )
        }
    }

    /**
     * 3. 채팅방 목록 조회
     * - 사용자가 속한 모든 채팅방
     * - 각 방의 마지막 메시지, 읽지 않은 메시지 개수 포함
     */
    fun getChatRoomList(currentUserId: Long): ChatRoomListResponse {
        return transaction {
            // 내가 속한 채팅방 ID 목록
            val roomIds = memberChatRoomRepository.findRoomIdsByMemberId(currentUserId)

            if (roomIds.isEmpty()) {
                return@transaction ChatRoomListResponse(emptyList())
            }

            // 채팅방 정보 조회
            val chatRooms = chatRoomRepository.findByIds(roomIds)

            // 각 방의 요약 정보 생성
            val summaries = chatRooms.map { room ->
                // 채팅방 멤버 조회
                val members = memberChatRoomRepository.findMembersByRoomId(room.id)

                // DIRECT 채팅방은 상대방 이름으로 표시
                val displayName = if (room.roomType == RoomType.DIRECT) {
                    val partner = members.find { it.userId != currentUserId }
                    partner?.username ?: "알 수 없는 사용자"
                } else {
                    room.name ?: "Unnamed Room"
                }

                // 마지막 메시지 조회
                val lastMessage = chatMessageRepository.findLastMessageByRoomId(room.id)

                // 마지막으로 읽은 메시지 ID
                val lastReadMessageId = memberChatRoomRepository.findLastReadMessageId(room.id, currentUserId)

                // 읽지 않은 메시지 개수 계산
                val unreadCount = if (lastReadMessageId != null) {
                    chatMessageRepository.countUnreadMessages(
                        roomId = room.id,
                        afterMessageId = lastReadMessageId,
                        excludeSenderId = currentUserId
                    )
                } else {
                    // 아직 하나도 안 읽음 (전체 메시지 개수, 단 본인 메시지 제외)
                    chatMessageRepository.countAllMessages(room.id, excludeSenderId = currentUserId)
                }

                ChatRoomSummary(
                    roomId = room.id,
                    roomType = room.roomType,
                    name = displayName,
                    lastMessage = lastMessage?.content,
                    unreadCount = unreadCount,
                    updatedAt = lastMessage?.createdAt
                )
            }

            ChatRoomListResponse(
                rooms = summaries.sortedByDescending { it.updatedAt }
            )
        }
    }

    /**
     * 4. 채팅방 상세 조회
     * - 특정 채팅방의 상세 정보와 멤버 목록 반환
     */
    fun getChatRoomDetail(roomId: Long, currentUserId: Long): ChatRoomDetailResponse {
        return transaction {
            // 채팅방 존재 확인
            val chatRoom = chatRoomRepository.findById(roomId)
                ?: throw BusinessException(ErrorCode.CHATROOM_NOT_FOUND)

            // 사용자가 해당 채팅방의 참여자인지 확인
            val memberIds = memberChatRoomRepository.findMemberIdsByRoomId(roomId)
            if (!memberIds.contains(currentUserId)) {
                throw BusinessException(ErrorCode.NOT_CHATROOM_MEMBER)
            }

            // 채팅방 멤버 조회 (role 포함)
            val membersWithRole = memberChatRoomRepository.findMembersWithRoleByRoomId(roomId)
            val members = membersWithRole.map { (memberInfo, role) ->
                ChatRoomMemberDetail(
                    userId = memberInfo.userId,
                    username = memberInfo.username,
                    profileImage = memberInfo.profileImageUrl,
                    role = role
                )
            }

            // DIRECT 채팅방은 상대방 이름으로 표시
            val displayName = if (chatRoom.roomType == RoomType.DIRECT) {
                val partner = members.find { it.userId != currentUserId }
                partner?.username ?: "알 수 없는 사용자"
            } else {
                chatRoom.name ?: "Unnamed Room"
            }

            ChatRoomDetailResponse(
                roomId = chatRoom.id,
                roomType = chatRoom.roomType,
                name = displayName,
                members = members,
                createdAt = chatRoom.createdAt
            )
        }
    }

    /**
     * 5. 메시지 읽음 처리
     * - 특정 메시지까지 읽음으로 표시
     */
    fun updateReadStatus(
        roomId: Long,
        request: UpdateReadStatusRequest,
        currentUserId: Long
    ): UpdateReadStatusResponse {
        return transaction {
            // 채팅방 존재 확인
            val chatRoom = chatRoomRepository.findById(roomId)
                ?: throw BusinessException(ErrorCode.CHATROOM_NOT_FOUND)

            // 사용자가 해당 채팅방의 참여자인지 확인
            val memberIds = memberChatRoomRepository.findMemberIdsByRoomId(roomId)
            if (!memberIds.contains(currentUserId)) {
                throw BusinessException(ErrorCode.NOT_CHATROOM_MEMBER)
            }

            // 읽음 상태 업데이트
            memberChatRoomRepository.updateLastReadMessageId(
                roomId = roomId,
                memberId = currentUserId,
                messageId = request.lastReadMessageId
            )

            UpdateReadStatusResponse(
                roomId = roomId,
                lastReadMessageId = request.lastReadMessageId
            )
        }
    }
}
