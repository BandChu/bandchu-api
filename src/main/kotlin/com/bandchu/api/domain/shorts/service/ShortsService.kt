package com.bandchu.api.domain.shorts.service

import com.bandchu.api.domain.member.repository.MemberRepository
import com.bandchu.api.domain.member.table.MemberTable
import com.bandchu.api.domain.shorts.dto.ShortsCommentResponse
import com.bandchu.api.domain.shorts.dto.ShortsResponse
import com.bandchu.api.domain.shorts.repository.ShortsRepository
import com.bandchu.api.domain.shorts.table.ShortsCommentTable
import com.bandchu.api.domain.shorts.table.ShortsLikeTable
import com.bandchu.api.domain.shorts.table.ShortsTable
import com.bandchu.api.global.config.S3Uploader
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
@Service
@Transactional
class ShortsService(
    private val shortsRepository: ShortsRepository,
    private val s3Uploader: S3Uploader,
    private val memberRepository: MemberRepository // 아티스트 ID 조회용으로 유지
) {
    // 1. 피드 조회
    fun getShortsFeed(lastId: Long?, size: Int, currentMemberId: Long?): List<ShortsResponse> {
        return shortsRepository.findFeed(lastId, size).map { row ->
            val shortsId = row[ShortsTable.id].value
            val isLiked = currentMemberId?.let { shortsRepository.isLiked(shortsId, it) } ?: false
            row.toResponse(isLiked) // row.toResponse는 ResultRow 확장 함수로 구현 권장
        }
    }

    // 2. 릴스 업로드
    fun upload(memberId: Long, file: MultipartFile, title: String, desc: String?): Long {
        val videoUrl = s3Uploader.upload(file, "shorts/videos")
        val thumbUrl = "https://bandchu-bucket.s3.ap-northeast-2.amazonaws.com/default-thumb.jpg"

        // ArtistProfileId 가져오는 로직 (기존 로직 활용)
        val artistId = getArtistIdByMemberId(memberId)

        return shortsRepository.insertShorts(artistId, title, desc, videoUrl, thumbUrl)
    }

    // 3. 좋아요 토글
    fun toggleLike(shortsId: Long, memberId: Long): Boolean {
        return if (shortsRepository.isLiked(shortsId, memberId)) {
            shortsRepository.deleteLike(shortsId, memberId)
            false
        } else {
            shortsRepository.insertLike(shortsId, memberId)
            true
        }
    }

    // 4. 댓글 작성
    fun createComment(shortsId: Long, memberId: Long, content: String): Long {
        return shortsRepository.insertComment(shortsId, memberId, content)
    }

    // 5. 댓글 목록 조회
    fun getComments(shortsId: Long): List<ShortsCommentResponse> {
        return shortsRepository.findCommentsWithMember(shortsId).map { row ->
            ShortsCommentResponse(
                commentId = row[ShortsCommentTable.id].value,
                nickname = row[MemberTable.nickname],
                content = row[ShortsCommentTable.content],
                createdAt = row[ShortsCommentTable.createdAt]
            )
        }
    }

    // 7. 조회수 증가 (컨트롤러에서 이걸 부르게 함)
    fun addViewCount(shortsId: Long) {
        shortsRepository.incrementCount(shortsId, ShortsTable.viewCount)
    }

    // 7. 공유수 증가 (컨트롤러에서 이걸 부르게 함)
    fun incrementShareCount(shortsId: Long) {
        shortsRepository.incrementCount(shortsId, ShortsTable.shareCount)
    }
    // 6. 댓글 삭제 (권한 체크 포함)
    fun deleteComment(commentId: Long, memberId: Long) {
        val comment = shortsRepository.findCommentById(commentId)
            ?: throw BusinessException(ErrorCode.COMMENT_NOT_FOUND)

        if (comment[ShortsCommentTable.memberId].value.toLong() != memberId.toLong()){
            throw BusinessException(ErrorCode.COMMENT_DELETE_NOT_ALLOWED)
        }
        shortsRepository.deleteComment(commentId)
    }


    // Helper: 아티스트 ID 조회 (구현 필요)
    private fun getArtistIdByMemberId(memberId: Long): Long {
        // artistProfileRepository 등을 통해 가져오는 로직
        return 1L // 임시
    }
    fun ResultRow.toResponse(isLiked: Boolean): ShortsResponse {
        return ShortsResponse(
            id = this[ShortsTable.id].value,
            artistName = "Artist", // 나중에 Join해서 가져오도록 수정
            title = this[ShortsTable.title],
            description = this[ShortsTable.description],
            videoUrl = this[ShortsTable.videoUrl],
            thumbnailUrl = this[ShortsTable.thumbnailUrl],
            viewCount = this[ShortsTable.viewCount],
            shareCount = this[ShortsTable.shareCount],
            isLiked = isLiked,
            createdAt = this[ShortsTable.createdAt]
        )
    }

}

