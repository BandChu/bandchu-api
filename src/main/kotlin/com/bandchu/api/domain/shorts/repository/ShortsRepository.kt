package com.bandchu.api.domain.shorts.repository

import com.bandchu.api.domain.member.table.MemberTable
import com.bandchu.api.domain.shorts.table.ShortsCommentTable
import com.bandchu.api.domain.shorts.table.ShortsLikeTable
import com.bandchu.api.domain.shorts.table.ShortsTable
import org.jetbrains.exposed.v1.core.SortOrder

import org.jetbrains.exposed.v1.core.* import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class ShortsRepository {

    // 1. 피드 조회 (No-offset 페이징)
    fun findFeed(lastId: Long?, size: Int) = transaction {
        val query = ShortsTable.selectAll()
        lastId?.let { query.where { ShortsTable.id less it } }

        query.orderBy(ShortsTable.id to SortOrder.DESC)
            .limit(size)
            .toList()
    }

    // 2. 좋아요 여부 확인
    fun isLiked(shortsId: Long, memberId: Long): Boolean = transaction {
        ShortsLikeTable.selectAll()
            .where { (ShortsLikeTable.shortsId eq shortsId) and (ShortsLikeTable.memberId eq memberId) }
            .count() > 0
    }

    // 3. 좋아요 추가/삭제
    fun insertLike(shortsId: Long, memberId: Long) = transaction {
        ShortsLikeTable.insert {
            it[this.shortsId] = shortsId
            it[this.memberId] = memberId
        }
    }

    fun deleteLike(shortsId: Long, memberId: Long) = transaction {
        ShortsLikeTable.deleteWhere { (ShortsLikeTable.shortsId eq shortsId) and (ShortsLikeTable.memberId eq memberId) }
    }

    // 4. 댓글 작성
    fun insertComment(shortsId: Long, memberId: Long, content: String) = transaction {
        ShortsCommentTable.insertAndGetId {
            it[this.shortsId] = shortsId
            it[this.memberId] = memberId
            it[this.content] = content
        }.value
    }

    // 5. 댓글 목록 조회 (작성자 정보 포함)
    fun findCommentsWithMember(shortsId: Long) = transaction {
        (ShortsCommentTable innerJoin MemberTable)
            .selectAll() // select() 대신 selectAll() 후 where 사용 권장 버전일 경우
            .where { ShortsCommentTable.shortsId eq shortsId }
            .orderBy(ShortsCommentTable.createdAt to SortOrder.DESC)
            .toList()
    }

    // 6. 카운트 증가 (조회수/공유수)
    fun incrementCount(shortsId: Long, column: Column<Long>) = transaction {
        ShortsTable.update({ ShortsTable.id eq shortsId }) {
            it.update(column, column plus 1L)
        }
    }
    // 6-1. 댓글 단건 조회 (서비스 80번 줄 빨간 줄 해결용)
    fun findCommentById(commentId: Long) = transaction {
        ShortsCommentTable.selectAll()
            .where { ShortsCommentTable.id eq commentId }
            .singleOrNull()
    }
    // 6-2. 댓글 삭제 (서비스 88번 줄 빨간 줄 해결용)
    fun deleteComment(commentId: Long) = transaction {
        ShortsCommentTable.deleteWhere { ShortsCommentTable.id eq commentId }
    }
    // 7. 릴스 업로드 저장
    fun insertShorts(artistId: Long, title: String, desc: String?, vUrl: String, tUrl: String?) = transaction {
        ShortsTable.insertAndGetId {
            it[this.artistProfileId] = artistId
            it[this.title] = title
            it[this.description] = desc
            it[this.videoUrl] = vUrl
            it[this.thumbnailUrl] = tUrl
        }.value
    }
}