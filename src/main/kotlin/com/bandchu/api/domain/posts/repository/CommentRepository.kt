package com.bandchu.api.domain.posts.repository

import com.bandchu.api.domain.member.table.MemberTable
import com.bandchu.api.domain.posts.dto.CommentWithMember
import com.bandchu.api.domain.posts.dto.response.CommentResponse
import com.bandchu.api.domain.posts.dto.response.toCommentResponse
import com.bandchu.api.domain.posts.table.CommentTable
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class CommentRepository {

    fun findByPostId(postId: Long): List<CommentResponse> = transaction {
        CommentTable
            .selectAll()
            .where{ CommentTable.postId eq postId }
            .map { it.toCommentResponse() }

        // 게시글은 있는데 댓글이 없는 경우 404라 확인해봄
//        if (comments.isEmpty()) {
//            throw BusinessException(ErrorCode.COMMENT_NOT_FOUND)
//        }
    }

    fun insertComment(memberId: Long, postId: Long, content: String): CommentResponse = transaction {
        val now = OffsetDateTime.now()

        val inserted = CommentTable.insert { row ->
            row[CommentTable.memberId] = memberId
            row[CommentTable.postId] = postId
            row[CommentTable.content] = content
            row[CommentTable.createdAt] = now
            row[CommentTable.updatedAt] = now
        }

        val id = inserted[CommentTable.id]
        findByCommentId(id) ?: throw BusinessException(ErrorCode.COMMENT_INSERT_FAILED)
    }

    fun updateComment(commentId: Long, content: String): CommentResponse = transaction {
        val updatedRows = CommentTable.update({ CommentTable.id eq commentId }) { row ->
            row[CommentTable.content] = content
            row[CommentTable.updatedAt] = OffsetDateTime.now()
        }

        if (updatedRows == 0) {
            throw BusinessException(ErrorCode.COMMENT_NOT_FOUND)
        }

        findByCommentId(commentId) ?: throw BusinessException(ErrorCode.COMMENT_UPDATE_FAILED)
    }

    fun deleteComment(commentId: Long) = transaction {
        val deletedRows = CommentTable.deleteWhere { CommentTable.id eq commentId }
        if (deletedRows == 0) {
            throw BusinessException(ErrorCode.COMMENT_NOT_FOUND)
        }
        commentId
    }

    fun findByCommentId(commentId: Long): CommentResponse? = transaction {
        CommentTable
            .selectAll()
            .where{ CommentTable.id eq commentId }
            .singleOrNull()
            ?.toCommentResponse()
    }

    fun findByPostIdWithMember(postId: Long): List<CommentWithMember> = transaction {
        (CommentTable innerJoin MemberTable)
            .select(
                CommentTable.id,
                CommentTable.postId,
                CommentTable.memberId,
                CommentTable.content,
                CommentTable.createdAt,
                CommentTable.updatedAt,
                MemberTable.nickname
            )
            .where { CommentTable.postId eq postId }
            .orderBy(CommentTable.createdAt to SortOrder.ASC) // 댓글 시간순
            .map { row ->
                CommentWithMember(
                    commentId = row[CommentTable.id],
                    postId = row[CommentTable.postId],
                    memberId = row[CommentTable.memberId],
                    memberName = row[MemberTable.nickname],
                    content = row[CommentTable.content],
                    createdAt = row[CommentTable.createdAt],
                    updatedAt = row[CommentTable.updatedAt]
                )
            }
    }

}
