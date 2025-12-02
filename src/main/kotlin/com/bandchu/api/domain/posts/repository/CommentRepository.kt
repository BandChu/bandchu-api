package com.bandchu.api.domain.posts.repository

import com.bandchu.api.domain.posts.dto.response.CommentResponse
import com.bandchu.api.domain.posts.dto.response.toCommentResponse
import com.bandchu.api.domain.posts.table.CommentTable
import com.bandchu.api.global.exception.BusinessException
import com.bandchu.api.global.exception.ErrorCode
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class CommentRepository {

    fun findByPostId(postId: Long): List<CommentResponse> = transaction {
        val comments = CommentTable
            .selectAll()
            .where{ CommentTable.postId eq postId }
            .map { it.toCommentResponse() }

        if (comments.isEmpty()) {
            throw BusinessException(ErrorCode.COMMENT_NOT_FOUND)
        }

        comments
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
}
